-- Non-destructive, repeatable migration. Set actual prices in Manage Services > Add-ons.
USE metric_laundry;
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

-- Enable previously unpriced starter items at the requested shop price.
UPDATE addons SET price=20.00, active=TRUE WHERE name IN ('Detergent powder','Fabric softener') AND price IS NULL;
