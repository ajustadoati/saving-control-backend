# Consolidated supply payments

1. Back up the savings database. Check that `pago.id` is signed `INT` (as in the original savings schema).
2. Apply `2026-11-16-supply-multipayment-link.sql` once in the selected savings database. It adds an optional link; it does not update historical rows or delete default payments.
3. Deploy the backend before the frontend. The new payment form sends `SUPPLIES_GLOBAL` without a reference; existing explicit `SUPPLIES` requests still require a supply ID. Both register one `SUMINISTROS` cash entry per submitted line.
4. In a test environment verify the block for a legacy default, remove it in Associate Setup, then pay and reverse one consolidated amount spanning two debts. Confirm a separate supply payment on the same date remains intact and that the daily report counts the consolidated amount only once.

New multipayment allocations reference their exact cash entry. Historical multipayments without a link still use the previous user/date reversal rule when a supply cash entry exists. Do not backfill historical links by date alone.

Do not remove the column to roll back the application. The old backend cannot distinguish standalone payments from linked allocations on reversal. If rollback is necessary after consolidated payments have been posted, defer reversing those batches until a link-aware backend is available.
