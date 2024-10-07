CREATE DATABASE IF NOT EXISTS a9i-savings;

USE a9i-savings;



CREATE TABLE role (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL
);

CREATE TABLE user (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    number_id VARCHAR(20),
    mobile_number VARCHAR(20),
    email VARCHAR(100),
    password VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_role (
    user_id INT,
    role_id INT,
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (role_id) REFERENCES role(role_id)
);


CREATE TABLE associate (
    associate_id INT AUTO_INCREMENT PRIMARY KEY,
    main_associate_id INT NOT NULL,
    guess_associate_id INT NOT NULL,
    relationship VARCHAR(50),
    FOREIGN KEY (main_associate_id) REFERENCES user(user_id),
    FOREIGN KEY (guess_associate_id) REFERENCES user(user_id)
);

CREATE TABLE saving (
    saving_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    saving_date DATE,
    amount DECIMAL(10, 2),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
);


CREATE TABLE loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    loan_amount DECIMAL(10, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    loan_balance DECIMAL(10, 2) NOT NULL,
    issued_at DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
);


CREATE TABLE loan_payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT,
    payment_date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (loan_id) REFERENCES loan(loan_id) ON DELETE CASCADE
);

CREATE TABLE funds (
    fund_id INT AUTO_INCREMENT PRIMARY KEY,
    initial_capital DECIMAL(12, 2) NOT NULL,
    current_balance DECIMAL(12, 2) NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


INSERT INTO `a9i-savings`.`role` (role_name) VALUES
	 ('ADMIN'),
	 ('ASSOCIATE');

INSERT INTO `a9i-savings`.`user` (first_name,last_name,number_id,mobile_number,email,created_at,password) VALUES
	 ('Richard','Rojas','14447876','2710698','richardroj@gmail.com','2024-10-06 14:12:21','$2a$10$P7nbymUqL/IbvX5NzpU/xeQ2jCAk0lzGeDAPnQ/6mNcl/NJCq6eR2');

INSERT INTO `a9i-savings`.`role` (role_name) VALUES
	 ('ADMIN');




