-- Run once on the savings database before deploying the new backend.
-- Historical rows and direct supply payments retain a NULL link.
ALTER TABLE supply_payment
    ADD COLUMN multipayment_pago_id INT NULL,
    ADD INDEX ix_supply_payment_multipayment_pago (multipayment_pago_id),
    ADD CONSTRAINT fk_supply_payment_multipayment_pago
        FOREIGN KEY (multipayment_pago_id) REFERENCES pago (id);
