-- Run once before deploying the new backend and frontend. Safe to rerun.
CREATE TABLE IF NOT EXISTS logic_balance (
  id BIGINT NOT NULL PRIMARY KEY,
  initial_balance DECIMAL(14,2) NOT NULL,
  current_balance DECIMAL(14,2) NOT NULL
) ENGINE=InnoDB;

INSERT INTO logic_balance (id, initial_balance, current_balance)
SELECT 1, 19524.00, 19524.00
WHERE NOT EXISTS (SELECT 1 FROM logic_balance WHERE id = 1);

CREATE TABLE IF NOT EXISTS logic_payment (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  request_id VARCHAR(36) NOT NULL UNIQUE,
  payment_date DATE NOT NULL,
  amount DECIMAL(14,2) NOT NULL,
  balance_after DECIMAL(14,2) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  created_by VARCHAR(255) NOT NULL
) ENGINE=InnoDB;
