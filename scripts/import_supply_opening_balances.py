#!/usr/bin/env python3
"""Dry-run first: import outstanding supplies, never sales, stock or cash movements."""
import argparse
import datetime as dt
from decimal import Decimal, ROUND_CEILING
import hashlib
import json
from pathlib import Path
import re
import subprocess
import sys


def literal(value):
    # Hex literals avoid SQL escaping and client/server charset ambiguity.
    return "CONVERT(0x" + str(value).encode('utf-8').hex() + " USING utf8mb4)"


def read_rows(path):
    try:
        import openpyxl
    except ImportError:
        raise ValueError('Reading XLSX requires openpyxl in the Python environment')
    sheet = openpyxl.load_workbook(path, data_only=True, read_only=True).worksheets[0]
    rows = []
    seen = set()
    for values in sheet.iter_rows(min_row=5, values_only=True):
        if not values[1]:
            continue
        number_id = str(values[2]).strip()
        if number_id.endswith('.0'):
            number_id = number_id[:-2]
        if not number_id.isdigit() or number_id in seen:
            raise ValueError('Missing/duplicate identification: ' + number_id)
        seen.add(number_id)
        balance = Decimal(str(values[12] or 0))
        if balance <= 0:
            continue
        quota = Decimal(str(values[10] or 0))
        if not balance.is_finite() or not quota.is_finite() or quota <= 0:
            raise ValueError('Invalid balance/quota for ' + number_id)
        if any(v != v.quantize(Decimal('.01')) for v in [balance, quota]):
            raise ValueError('More than two decimal places for ' + number_id)
        rows.append({'numberId': number_id, 'balance': balance, 'quota': quota})
    if not rows:
        raise ValueError('No debtors found')
    return rows


def apply_adjustments(rows, adjustments):
    audit = []
    seen = set()
    for value in adjustments:
        match = re.fullmatch(r'([0-9]+)=([+-]?[0-9]+(?:\.[0-9]{1,2})?)', value)
        if not match or match[1] in seen:
            raise ValueError('Invalid/duplicate balance adjustment: ' + value)
        number_id, amount = match[1], Decimal(match[2])
        row = next((row for row in rows if row['numberId'] == number_id), None)
        if row is None or amount == 0 or row['balance'] + amount <= 0:
            raise ValueError('Adjustment requires an existing debtor and a positive final balance: ' + value)
        original = row['balance']
        row['balance'] += amount
        seen.add(number_id)
        audit.append({'numberId': number_id, 'originalBalance': str(original),
                      'adjustment': str(amount), 'importBalance': str(row['balance']),
                      'weeklyInstallment': str(row['quota'])})
    return audit


class Database:
    def __init__(self, args):
        self.command = [args.mysql, '--defaults-extra-file=' + str(Path(args.defaults_file).resolve()),
                        '--protocol=TCP', '--host=' + args.host, '--port=' + str(args.port),
                        '--database=' + args.database, '--batch', '--raw', '--skip-column-names',
                        '--default-character-set=utf8mb4']

    def query(self, sql):
        result = subprocess.run(self.command, input=sql, text=True, capture_output=True)
        if result.returncode:
            raise ValueError('MySQL operation failed (transaction not committed): ' + result.stderr.strip())
        return [json.loads(line) for line in result.stdout.splitlines() if line.strip()]


def run(args):
    rows = read_rows(args.workbook)
    source_total = sum(row['balance'] for row in rows)
    if args.expected_source_total is not None and source_total != args.expected_source_total:
        raise ValueError(f'Source balances sum to {source_total}, expected {args.expected_source_total}')
    adjustments = apply_adjustments(rows, args.balance_adjustment)
    reconcile_ids = set(args.reconcile_existing)
    if not reconcile_ids.issubset({row['numberId'] for row in rows}):
        raise ValueError('Reconciliation identification must exist in the source')
    import_total = sum(row['balance'] for row in rows)
    if import_total != args.expected_total:
        raise ValueError(f'Adjusted balances sum to {import_total}, expected {args.expected_total}')
    if args.only_number_id:
        rows = [row for row in rows if row['numberId'] == args.only_number_id]
        if not rows:
            raise ValueError('Selected identification not found')
    database = Database(args)
    identity = database.query("SELECT JSON_OBJECT('uuid',@@server_uuid,'hostname',@@hostname,'database',DATABASE());")[0]
    if args.apply and (not args.expect_server_uuid or identity['uuid'] != args.expect_server_uuid):
        raise ValueError('Apply requires the exact --expect-server-uuid from the dry run')
    product = database.query("SELECT JSON_OBJECT('id',lubricant_product_id,'code',code,'name',name,'active',CAST(active AS UNSIGNED)) "
                             f"FROM lubricant_product WHERE lubricant_product_id={args.product_id};")
    if len(product) != 1 or not product[0]['active']:
        raise ValueError('Select an existing active placeholder product')
    pending, already, problems, reconciliations = [], [], [], []
    for row in rows:
        number_id = row['numberId']
        marker = f'SALDO INICIAL SUMINISTROS|{args.batch_id}|{number_id}'
        users = database.query("SELECT JSON_OBJECT('id',user_id) FROM user WHERE number_id=" + literal(number_id) + ';')
        if len(users) != 1:
            problems.append({'numberId': number_id, 'problem': 'Associate missing or ambiguous'})
            continue
        row.update(userId=users[0]['id'], marker=marker)
        previous = database.query("SELECT JSON_OBJECT('supplyId',s.supply_id,'orderId',o.lubricant_order_id,"
            "'initial',CAST(s.supply_amount AS CHAR),'quota',CAST(o.weekly_installment AS CHAR)) "
            "FROM supply s LEFT JOIN lubricant_order o ON o.supply_id=s.supply_id WHERE s.supply_name=" + literal(marker) + ';')
        if previous:
            if len(previous) != 1 or not previous[0]['orderId'] or Decimal(previous[0]['initial']) != row['balance'] or Decimal(previous[0]['quota']) != row['quota']:
                problems.append({'numberId': number_id, 'problem': 'Existing import differs from source'})
            else:
                already.append(number_id)
            continue
        conflicts = database.query("SELECT JSON_OBJECT('id',s.supply_id,'balance',CAST(s.supply_balance AS CHAR),"
            "'orderId',o.lubricant_order_id,'payments',(SELECT COUNT(*) FROM supply_payment p WHERE p.supply_id=s.supply_id)) "
            "FROM supply s LEFT JOIN lubricant_order o ON o.supply_id=s.supply_id "
            f"WHERE s.user_id={row['userId']} AND s.supply_balance>0;")
        if conflicts:
            if number_id not in reconcile_ids or any(supply['orderId'] is not None for supply in conflicts):
                problems.append({'numberId': number_id, 'problem': 'Existing outstanding supplies; reconcile before import', 'supplies': conflicts})
                continue
            row['closeSupplies'] = conflicts
            reconciliations.append({'numberId': number_id, 'previousBalance': str(sum(Decimal(s['balance']) for s in conflicts)),
                                    'replacementBalance': str(row['balance']), 'supplies': conflicts,
                                    'preservePayments': True})
        pending.append(row)
    report = {'mode': 'apply' if args.apply else 'dry-run', 'server': identity,
              'sourceSha256': hashlib.sha256(Path(args.workbook).read_bytes()).hexdigest(),
              'sourceTotal': str(source_total), 'importTotal': str(import_total), 'adjustments': adjustments,
              'reconciliations': reconciliations,
              'selectedRows': len(rows), 'pending': len(pending),
              'pendingBalance': str(sum(row['balance'] for row in pending)), 'alreadyImported': already,
              'problems': problems, 'product': product[0], 'firstPaymentDate': args.first_payment_date,
              'stockAndCashModified': False}
    print(json.dumps(report, indent=2, default=str))
    if problems:
        return 2
    if not args.apply or not pending:
        return 0
    sql = ["SELECT GET_LOCK('supply-opening-balances-import',10) INTO @locked;",
           "CREATE TEMPORARY TABLE import_guard (ok INT NOT NULL CHECK (ok=1));",
           "INSERT INTO import_guard VALUES (@locked);", "START TRANSACTION;"]
    for row in pending:
        uid = row['userId']
        sql += [f'SELECT user_id INTO @import_user FROM user WHERE user_id={uid} FOR UPDATE;']
        for previous in row.get('closeSupplies', []):
            sid, previous_balance = previous['id'], Decimal(previous['balance'])
            # Lock and verify the reviewed balance; a concurrent payment must abort the whole import.
            sql += [f'SELECT supply_id INTO @close_supply FROM supply WHERE supply_id={sid} FOR UPDATE;',
                    'INSERT INTO import_guard SELECT IF(COUNT(*)=1,1,0) FROM supply '
                    f'WHERE supply_id={sid} AND user_id={uid} AND supply_balance={previous_balance};',
                    'INSERT INTO import_guard SELECT IF(COUNT(*)=0,1,0) FROM lubricant_order '
                    f'WHERE supply_id={sid};',
                    'INSERT INTO balance_history(user_id,transaction_date,transaction_type,amount,description) VALUES '
                    f"({uid},{literal(args.first_payment_date)},'SUPPLIES',{-previous_balance},"
                    f"{literal(f'CONCILIACION NO PAGO | {args.batch_id} | cierre supply #{sid}; reemplazado por saldo inicial Excel')});",
                    'UPDATE supply SET supply_balance=0,supply_name=CONCAT(LEFT(COALESCE(supply_name,\'\'),180),'
                    f"{literal(' | CERRADO POR SALDO INICIAL ' + args.batch_id)}) WHERE supply_id={sid} AND user_id={uid};"]
        sql += ['INSERT INTO import_guard SELECT IF(COUNT(*)=0,1,0) FROM supply WHERE '
                f'user_id={uid} AND (supply_balance>0 OR supply_name={literal(row["marker"])});']
        amount, quota = row['balance'], row['quota']
        weeks = int((amount / quota).to_integral_value(rounding=ROUND_CEILING))
        date = literal(args.first_payment_date)
        sql += [f'INSERT INTO supply(user_id,supply_amount,supply_balance,supply_date,supply_name) VALUES '
                f'({uid},{amount},{amount},{date},{literal(row["marker"])});',
                'SET @supply_id=LAST_INSERT_ID();',
                'INSERT INTO lubricant_order(user_id,order_date,total_amount,balance,weekly_installment,installment_count,status,supply_id,created_at) VALUES '
                f"({uid},{date},{amount},{amount},{quota},{weeks},'PENDING',@supply_id,NOW());",
                'SET @order_id=LAST_INSERT_ID();',
                'INSERT INTO lubricant_order_item(lubricant_order_id,lubricant_product_id,product_code,product_name,quantity,unit_price,line_total) VALUES '
                f'(@order_id,{args.product_id},{literal(product[0]["code"])},{literal("SALDO INICIAL - " + product[0]["name"])},1,{amount},{amount});']
    sql += ["COMMIT;", "DO RELEASE_LOCK('supply-opening-balances-import');"]
    database.query('\n'.join(sql))
    print(json.dumps({'committed': len(pending), 'balance': str(sum(row['balance'] for row in pending))}))
    return 0


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('workbook')
    p.add_argument('--defaults-file', required=True, help='Private MySQL [client] credentials file (not committed)')
    p.add_argument('--host', choices=['127.0.0.1', 'localhost'], default='127.0.0.1')
    p.add_argument('--port', type=int, default=3307)
    p.add_argument('--database', default='calch')
    p.add_argument('--mysql', default='mysql')
    p.add_argument('--product-id', type=int, required=True)
    p.add_argument('--batch-id', required=True)
    p.add_argument('--first-payment-date', required=True)
    p.add_argument('--expected-total', type=Decimal, required=True)
    p.add_argument('--expected-source-total', type=Decimal)
    p.add_argument('--balance-adjustment', action='append', default=[], metavar='NUMBER_ID=AMOUNT',
                   help='Explicit audited adjustment, without modifying the workbook (repeatable)')
    p.add_argument('--only-number-id')
    p.add_argument('--reconcile-existing', action='append', default=[], metavar='NUMBER_ID',
                   help='Explicitly replace old unlinked balances, preserving payments and auditing the closure')
    p.add_argument('--expect-server-uuid')
    p.add_argument('--apply', action='store_true')
    args = p.parse_args()
    if args.product_id <= 0 or not re.fullmatch(r'[a-zA-Z0-9_-]{1,80}', args.batch_id):
        p.error('Invalid product/batch identification')
    if dt.date.fromisoformat(args.first_payment_date).weekday() != 2:
        p.error('First payment date must be a Wednesday')
    try:
        return run(args)
    except (ValueError, OSError) as exc:
        print(str(exc), file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())
