CREATE DATABASE IF NOT EXISTS a9i-savings;

USE a9i-savings;



CREATE TABLE role (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL
);

CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `number_id` varchar(20) DEFAULT NULL,
  `mobile_number` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(100) DEFAULT NULL,
  `company` varchar(100) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_unique` (`number_id`),
  UNIQUE KEY `user_unique_1` (`email`)
);

CREATE TABLE user_role (
    user_id INT,
    role_id INT,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (role_id) REFERENCES role(role_id)
);

CREATE TABLE default_payment (
    user_id INT,
    payment_name varchar(100) DEFAULT NULL,
    amount DECIMAL(10, 2),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE user_associate (
    associate_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    user_associate_id INT NOT NULL,
    relationship VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (user_associate_id) REFERENCES user(user_id)
);

CREATE TABLE saving (
    saving_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    saving_date DATE,
    amount DECIMAL(10, 2),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE loan_type (
    loan_type_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_type_name VARCHAR(50) NOT NULL
);

CREATE TABLE loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    loan_amount DECIMAL(10, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    loan_balance DECIMAL(10, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    loan_type_id INT,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (loan_type_id) REFERENCES loan_type(loan_type_id)
);

CREATE TABLE loan_payment_type (
    loan_payment_type_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_payment_type_name VARCHAR(50) NOT NULL
);

CREATE TABLE loan_payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT,
    payment_date DATE NOT NULL,
    payment_type_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (loan_id) REFERENCES loan(loan_id),
    FOREIGN KEY (payment_type_id) REFERENCES loan_payment_type(loan_payment_type_id)
);

CREATE TABLE product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE funds (
    fund_id INT AUTO_INCREMENT PRIMARY KEY,
    initial_capital DECIMAL(12, 2) NOT NULL,
    current_balance DECIMAL(12, 2) NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE contribution_type (
    contribution_type_id INT AUTO_INCREMENT PRIMARY KEY,
    contribution_type_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE contribution (
    contribution_id INT AUTO_INCREMENT PRIMARY KEY,
    contribution_type_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    contribution_date DATE NOT NULL,
    description VARCHAR(255),
    FOREIGN KEY (contribution_type_id) REFERENCES contribution_type(contribution_type_id)
);

CREATE TABLE contribution_payment (
    contribution_payment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    contribution_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (contribution_id) REFERENCES contribution(contribution_id)
);

CREATE TABLE other_payment (
    other_payment_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    name VARCHAR(200),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE supply (
    supply_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    supply_amount DECIMAL(10, 2) NOT NULL,
    supply_balance DECIMAL(10, 2) NOT NULL,
    supply_date DATE NOT NULL,
    supply_name VARCHAR(300),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE supply_payment (
    supply_payment_id INT AUTO_INCREMENT PRIMARY KEY,
    supply_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (supply_id) REFERENCES supply(supply_id)
);

CREATE TABLE pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    cedula VARCHAR(20) NOT NULL,
    tipo_pago VARCHAR(50) NOT NULL,
    monto DECIMAL(10, 2) NOT NULL
);

CREATE TABLE user_savings_box (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    box_count INT NOT NULL DEFAULT 0,
    box_value DECIMAL(10,2) NOT NULL,  -- Ejemplo: 18.00
    updated_at  DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE user_account_summary (
    summary_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    initial_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    current_balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

CREATE TABLE balance_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    transaction_date DATETIME NOT NULL,       -- Fecha y hora en que se realizó la transacción
    transaction_type VARCHAR(50) NOT NULL,      -- Tipo de transacción (p.ej., "Intereses", "Depósito", "Retiro", etc.)
    amount DECIMAL(12,2) NOT NULL,              -- Monto de la transacción (positivo para crédito, negativo para débito)
    previous_balance DECIMAL(12,2) NOT NULL,    -- Saldo antes de la transacción
    new_balance DECIMAL(12,2) NOT NULL,         -- Saldo después de la transacción
    description VARCHAR(255),                   -- Descripción o notas adicionales (opcional)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- Fecha de creación del registro
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);

INSERT INTO `role` (role_name) VALUES
	 ('ADMIN'),
	 ('ASSOCIATE'),
	 ('USER_ASSOCIATE');

INSERT INTO `user` (first_name,last_name,number_id,mobile_number,email,created_at,password) VALUES
	 ('Richard','Rojas','14447876','2710698','richardroj@gmail.com','2024-10-06 14:12:21','$2a$10$P7nbymUqL/IbvX5NzpU/xeQ2jCAk0lzGeDAPnQ/6mNcl/NJCq6eR2');

INSERT INTO `role` (role_name) VALUES
	 ('ADMIN');




