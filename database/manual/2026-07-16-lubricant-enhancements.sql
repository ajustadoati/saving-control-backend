ALTER TABLE lubricant_order
    ADD COLUMN installment_count INT NOT NULL DEFAULT 4 AFTER weekly_installment;
