-- Upgrade the legacy email-login schema to the Java desktop schema.
-- Run ONCE against the old schema only; first make a mysqldump backup.
-- Keeps original tables as legacy_* and preserves IDs, passwords and orders.
USE metric_laundry;
RENAME TABLE users TO legacy_users, services TO legacy_services,
 orders TO legacy_orders, order_status_history TO legacy_order_status_history,
 payments TO legacy_payments;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    CONSTRAINT desktop_fk_orders_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT desktop_fk_orders_service  FOREIGN KEY (service_id)  REFERENCES services(id),
    CONSTRAINT desktop_fk_orders_creator  FOREIGN KEY (created_by)  REFERENCES users(id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    CONSTRAINT desktop_fk_hist_order FOREIGN KEY (order_id)  REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT desktop_fk_hist_user  FOREIGN KEY (changed_by) REFERENCES users(id),
    INDEX idx_hist_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- PAYMENTS : one payment record per order.
-- Card numbers are NEVER stored (PCI-DSS) - card is paid on the POS terminal.
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    receipt_no    VARCHAR(20) NOT NULL UNIQUE,
    order_id      INT NOT NULL UNIQUE,
    method        ENUM('GCASH','CASH','CARD') NOT NULL,
    amount        DECIMAL(10,2) NOT NULL,
    reference_no  VARCHAR(50),
    status        ENUM('PENDING','PAID') NOT NULL DEFAULT 'PENDING',
    received_by   INT,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at       DATETIME,
    CONSTRAINT desktop_fk_pay_order FOREIGN KEY (order_id)    REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT desktop_fk_pay_user  FOREIGN KEY (received_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------

START TRANSACTION;
INSERT INTO users (id,username,password_hash,salt,full_name,email,phone,role,created_at)
SELECT user_id,
 CASE WHEN email='admin@metriclaundry.com' AND role='Admin' THEN 'admin'
      WHEN email='staff@metriclaundry.com' AND role='Staff' THEN 'staff'
      ELSE CONCAT('customer',user_id) END,
 password_hash, SUBSTRING_INDEX(SUBSTRING_INDEX(password_hash,'$',3),'$',-1),
 full_name,email,phone,UPPER(role),created_at FROM legacy_users;

INSERT INTO services (name,price,unit,description,turnaround_hours,active)
SELECT service_name,rate_per_kg,'KG',description,turnaround_days*24,is_available
FROM legacy_services ORDER BY sort_order,service_code;

INSERT INTO orders (id,order_no,customer_id,service_id,quantity,unit_price,total,status,notes,estimated_completion,created_at,updated_at)
SELECT o.order_id,CONCAT('ML-LEGACY-',o.order_id),o.customer_id,s.id,o.weight_kg,o.rate_per_kg,o.total_amount,
 CASE o.order_status WHEN 'Ready for Pickup' THEN 'READY' WHEN 'Claimed' THEN 'COMPLETED' ELSE UPPER(o.order_status) END,
 o.staff_note,o.expected_completion,o.order_date,o.order_date
FROM legacy_orders o JOIN legacy_services ls ON ls.service_code=o.service_code JOIN services s ON s.name=ls.service_name;

INSERT INTO order_status_history (id,order_id,status,changed_by,changed_at)
SELECT history_id,order_id,
 CASE status WHEN 'Ready for Pickup' THEN 'READY' WHEN 'Claimed' THEN 'COMPLETED' ELSE UPPER(status) END,
 changed_by,changed_at FROM legacy_order_status_history;

INSERT INTO payments (id,receipt_no,order_id,method,amount,reference_no,status,received_by,created_at,paid_at)
SELECT payment_id,CONCAT('RC-LEGACY-',payment_id),order_id,
 CASE payment_method WHEN 'Cash' THEN 'CASH' WHEN 'GCash' THEN 'GCASH' ELSE 'GCASH' END,
 amount,payment_reference,CASE WHEN payment_status='Paid' THEN 'PAID' ELSE 'PENDING' END,
 verified_by,COALESCE(paid_at,(SELECT order_date FROM legacy_orders o WHERE o.order_id=p.order_id)),paid_at
FROM legacy_payments p;
-- Bank Transfer has no desktop equivalent; retained as GCASH with its reference.
-- The exact original method remains in legacy_payments.

-- Add missing demo accounts without resetting any existing account password.
INSERT IGNORE INTO users (username, password_hash, salt, full_name, email, phone, address, role) VALUES
('admin', 'PkM67YhIw81PTaHWpunGioHae/EWZeDiJc08Q+Mbbf0=', 'TWV0cmljQWRtaW5TYWx0MQ==', 'System Administrator', 'admin@metriclaundry.ph', '09170000001', 'Kalawag 1, back of Water District', 'ADMIN'),
('staff', '9xGwvapVWHKwgjY/VIskcKCTKCshdEmZPE2kbDHChJQ=', 'TWV0cmljU3RhZmZTYWx0MQ==', 'Ana Santos', 'staff@metriclaundry.ph', '09170000002', 'Kalawag 1', 'STAFF'),
('juan',  'JI21qDh6j3G2mULK3/kOvLadnG4aHme2m0BubGeEN8k=', 'TWV0cmljQ3VzdFNhbHQwMQ==', 'Juan Dela Cruz', 'juan@email.com', '09181234567', 'Poblacion, Isulan', 'CUSTOMER'),
('maria', 'fylIJvG/5+N0B43xcJ478mZe+Y3rNs3MzMhYWHSbwuI=', 'TWV0cmljQ3VzdFNhbHQwMg==', 'Maria Reyes', 'maria@email.com', '09191234567', 'Kalawag 2, Isulan', 'CUSTOMER');
COMMIT;
