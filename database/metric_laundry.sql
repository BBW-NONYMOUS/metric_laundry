-- =====================================================================
--  METRIC LAUNDRY - Laundry Management System
--  Database: MySQL 8.x / MariaDB 10.6+
--  Run:  mysql -u root -p < metric_laundry.sql   (or import in phpMyAdmin)
-- =====================================================================
DROP DATABASE IF EXISTS metric_laundry;
CREATE DATABASE metric_laundry CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE metric_laundry;

-- ---------------------------------------------------------------------
-- USERS : one table for all three roles (ADMIN, STAFF, CUSTOMER)
-- Passwords are PBKDF2-HMAC-SHA256 (65,536 iterations), never plain text.
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    salt           VARCHAR(255) NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(100),
    phone          VARCHAR(20),
    address        VARCHAR(255),
    role           ENUM('ADMIN','STAFF','CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    active         TINYINT(1) NOT NULL DEFAULT 1,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_role (role)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- SERVICES : price list (Washing, Dry Cleaning, Folding, ...)
-- ---------------------------------------------------------------------
CREATE TABLE services (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(60)  NOT NULL UNIQUE,
    price             DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    unit              ENUM('KG','PC') NOT NULL,
    description       VARCHAR(255),
    turnaround_hours  INT NOT NULL DEFAULT 2,
    active            TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- ORDERS : unit_price is a SNAPSHOT so later price changes never
-- alter old orders / receipts.
-- ---------------------------------------------------------------------
CREATE TABLE orders (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    order_no              VARCHAR(20) NOT NULL UNIQUE,
    customer_id           INT NOT NULL,
    service_id            INT NOT NULL,
    quantity              DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    unit_price            DECIMAL(10,2) NOT NULL,
    total                 DECIMAL(10,2) NOT NULL,
    status                ENUM('PLACED','RECEIVED','WASHING','DRYING','READY','COMPLETED','CANCELLED')
                          NOT NULL DEFAULT 'PLACED',
    notes                 VARCHAR(255),
    estimated_completion  DATETIME,
    created_by            INT,
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_orders_service  FOREIGN KEY (service_id)  REFERENCES services(id),
    CONSTRAINT fk_orders_creator  FOREIGN KEY (created_by)  REFERENCES users(id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created (created_at)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- ORDER_STATUS_HISTORY : audit trail -> powers the "Track Your Laundry"
-- timeline (who changed what, and when).
-- ---------------------------------------------------------------------
CREATE TABLE order_status_history (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    order_id    INT NOT NULL,
    status      ENUM('PLACED','RECEIVED','WASHING','DRYING','READY','COMPLETED','CANCELLED') NOT NULL,
    changed_by  INT,
    remarks     VARCHAR(255),
    changed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hist_order FOREIGN KEY (order_id)  REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_hist_user  FOREIGN KEY (changed_by) REFERENCES users(id),
    INDEX idx_hist_order (order_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- PAYMENTS : one payment record per order.
-- Card numbers are NEVER stored (PCI-DSS) - card is paid on the POS terminal.
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    receipt_no    VARCHAR(20) NOT NULL UNIQUE,
    order_id      INT NOT NULL,
    method        ENUM('GCASH','CASH','CARD') NOT NULL,
    amount        DECIMAL(10,2) NOT NULL,
    reference_no  VARCHAR(50),
    status        ENUM('PENDING','PAID','VOID') NOT NULL DEFAULT 'PENDING',
    received_by   INT,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at       DATETIME,
    CONSTRAINT fk_pay_order FOREIGN KEY (order_id)    REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_pay_user  FOREIGN KEY (received_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- SEED DATA
-- Default logins:  admin / admin123   staff / staff123
--                  juan  / customer123   maria / customer123
-- ---------------------------------------------------------------------
INSERT INTO users (id, username, password_hash, salt, full_name, email, phone, address, role) VALUES
(1, 'admin', 'PkM67YhIw81PTaHWpunGioHae/EWZeDiJc08Q+Mbbf0=', 'TWV0cmljQWRtaW5TYWx0MQ==', 'System Administrator', 'admin@metriclaundry.ph', '09170000001', 'Kalawag 1, back of Water District', 'ADMIN'),
(2, 'staff', '9xGwvapVWHKwgjY/VIskcKCTKCshdEmZPE2kbDHChJQ=', 'TWV0cmljU3RhZmZTYWx0MQ==', 'Ana Santos', 'staff@metriclaundry.ph', '09170000002', 'Kalawag 1', 'STAFF'),
(3, 'juan',  'JI21qDh6j3G2mULK3/kOvLadnG4aHme2m0BubGeEN8k=', 'TWV0cmljQ3VzdFNhbHQwMQ==', 'Juan Dela Cruz', 'juan@email.com', '09181234567', 'Poblacion, Isulan', 'CUSTOMER'),
(4, 'maria', 'fylIJvG/5+N0B43xcJ478mZe+Y3rNs3MzMhYWHSbwuI=', 'TWV0cmljQ3VzdFNhbHQwMg==', 'Maria Reyes', 'maria@email.com', '09191234567', 'Kalawag 2, Isulan', 'CUSTOMER');

INSERT INTO services (id, name, price, unit, description, turnaround_hours) VALUES
(1, 'Washing',      30.00, 'KG', 'Fresh and clean',  2),
(2, 'Dry Cleaning', 50.00, 'PC', 'Gentle care',      24),
(3, 'Folding',      20.00, 'KG', 'Neatly folded',    1);

-- Sample orders spread over the last 7 days (feeds the Sales Overview chart)
INSERT INTO orders (id, order_no, customer_id, service_id, quantity, unit_price, total, status, estimated_completion, created_by, created_at) VALUES
(1, 'ML-SAMPLE001', 3, 1, 5,  30.00, 150.00, 'COMPLETED', NOW() - INTERVAL 6 DAY, 3, NOW() - INTERVAL 6 DAY - INTERVAL 2 HOUR),
(2, 'ML-SAMPLE002', 4, 2, 4,  50.00, 200.00, 'COMPLETED', NOW() - INTERVAL 5 DAY, 4, NOW() - INTERVAL 5 DAY - INTERVAL 3 HOUR),
(3, 'ML-SAMPLE003', 3, 3, 8,  20.00, 160.00, 'COMPLETED', NOW() - INTERVAL 4 DAY, 2, NOW() - INTERVAL 4 DAY - INTERVAL 1 HOUR),
(4, 'ML-SAMPLE004', 4, 1, 12, 30.00, 360.00, 'COMPLETED', NOW() - INTERVAL 3 DAY, 4, NOW() - INTERVAL 3 DAY - INTERVAL 2 HOUR),
(5, 'ML-SAMPLE005', 3, 2, 9,  50.00, 450.00, 'COMPLETED', NOW() - INTERVAL 2 DAY, 3, NOW() - INTERVAL 2 DAY - INTERVAL 5 HOUR),
(6, 'ML-SAMPLE006', 4, 1, 18, 30.00, 540.00, 'READY',     NOW() - INTERVAL 1 DAY, 2, NOW() - INTERVAL 1 DAY - INTERVAL 2 HOUR),
(7, 'ML-SAMPLE007', 3, 1, 2,  30.00, 60.00,  'WASHING',   NOW() + INTERVAL 2 HOUR, 3, NOW() - INTERVAL 1 HOUR);

INSERT INTO order_status_history (order_id, status, changed_by, changed_at) VALUES
(7, 'PLACED',   3, NOW() - INTERVAL 60 MINUTE),
(7, 'RECEIVED', 2, NOW() - INTERVAL 55 MINUTE),
(7, 'WASHING',  2, NOW() - INTERVAL 40 MINUTE),
(6, 'PLACED',   2, NOW() - INTERVAL 1 DAY - INTERVAL 2 HOUR),
(6, 'READY',    2, NOW() - INTERVAL 1 DAY);

INSERT INTO payments (receipt_no, order_id, method, amount, reference_no, status, received_by, created_at, paid_at) VALUES
('RC-SAMPLE001', 1, 'CASH',  150.00, NULL,          'PAID', 2, NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
('RC-SAMPLE002', 2, 'GCASH', 200.00, '1009283745',  'PAID', 2, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
('RC-SAMPLE003', 3, 'CASH',  160.00, NULL,          'PAID', 2, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
('RC-SAMPLE004', 4, 'CARD',  360.00, NULL,          'PAID', 2, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
('RC-SAMPLE005', 5, 'GCASH', 450.00, '1009283999',  'PAID', 2, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
('RC-SAMPLE007', 7, 'GCASH', 60.00,  '1009284100',  'PAID', 2, NOW() - INTERVAL 50 MINUTE, NOW() - INTERVAL 50 MINUTE);

-- Optional extras; initial shop price PHP 20 per sachet, editable by admin.
CREATE TABLE IF NOT EXISTS addons (
 id INT AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(100) NOT NULL UNIQUE,
 unit VARCHAR(30) NOT NULL,
 price DECIMAL(10,2) NULL,
 active BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS order_addons (
 id INT AUTO_INCREMENT PRIMARY KEY,
 order_id INT NOT NULL,
 addon_id INT NOT NULL,
 name VARCHAR(100) NOT NULL,
 unit VARCHAR(30) NOT NULL,
 quantity DECIMAL(10,2) NOT NULL,
 unit_price DECIMAL(10,2) NOT NULL,
 CONSTRAINT fk_addon_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
 CONSTRAINT fk_addon_catalog FOREIGN KEY (addon_id) REFERENCES addons(id),
 UNIQUE KEY order_addon (order_id,addon_id)
) ENGINE=InnoDB;
INSERT IGNORE INTO addons (name,unit,price,active) VALUES
 ('Detergent powder','sachet',20.00,TRUE),
 ('Fabric softener','sachet',20.00,TRUE);
