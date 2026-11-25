ALTER TABLE orders ADD COLUMN IF NOT EXISTS store_location varchar(255);

UPDATE orders
SET store_location = 'Manipal'
WHERE store_location IS NULL OR BTRIM(store_location) = '';

ALTER TABLE orders ALTER COLUMN store_location SET DEFAULT 'Manipal';
ALTER TABLE orders ALTER COLUMN store_location SET NOT NULL;
