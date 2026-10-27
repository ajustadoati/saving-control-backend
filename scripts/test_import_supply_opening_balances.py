import unittest
from decimal import Decimal
from import_supply_opening_balances import apply_adjustments


class BalanceAdjustmentsTest(unittest.TestCase):
    def rows(self):
        return [{'numberId': '999000001', 'balance': Decimal('1000'), 'quota': Decimal('301')}]

    def test_authorized_adjustment_preserves_quota_and_audits_original(self):
        rows = self.rows()
        audit = apply_adjustments(rows, ['999000001=90'])
        self.assertEqual(rows[0]['balance'], Decimal('1090'))
        self.assertEqual(rows[0]['quota'], Decimal('301'))
        self.assertEqual(audit[0]['originalBalance'], '1000')
        self.assertEqual(audit[0]['adjustment'], '90')

    def test_unknown_associate_is_rejected(self):
        with self.assertRaises(ValueError):
            apply_adjustments(self.rows(), ['999=90'])

    def test_duplicate_adjustment_is_rejected(self):
        with self.assertRaises(ValueError):
            apply_adjustments(self.rows(), ['999000001=90', '999000001=90'])

    def test_invalid_amount_or_nonpositive_final_balance_is_rejected(self):
        for value in ['999000001=NaN', '999000001=0', '999000001=90.001', '999000001=-1000']:
            with self.subTest(value=value), self.assertRaises(ValueError):
                apply_adjustments(self.rows(), [value])


if __name__ == '__main__':
    unittest.main()
