BEGIN;

-- Schema bootstrap: create tables if missing so seed can run on a brand-new database.
CREATE TABLE IF NOT EXISTS languages (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(255),
  name VARCHAR(255),
  locale_code VARCHAR(255) NOT NULL,
  rtl BOOLEAN NOT NULL DEFAULT FALSE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  default_language BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS currencies (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(255),
  name VARCHAR(255),
  symbol VARCHAR(255),
  locale_code VARCHAR(255),
  exchange_rate_to_default NUMERIC(19, 4) NOT NULL,
  decimal_digits INTEGER NOT NULL,
  symbol_position VARCHAR(255) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  default_currency BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS categories (
  id BIGSERIAL PRIMARY KEY,
  name_en VARCHAR(255),
  name_vi VARCHAR(255),
  name_fr VARCHAR(255),
  name_ar VARCHAR(255),
  name_de VARCHAR(255),
  name_ja VARCHAR(255),
  name_ru VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS products (
  id BIGSERIAL PRIMARY KEY,
  name_en VARCHAR(255),
  name_vi VARCHAR(255),
  name_fr VARCHAR(255),
  name_ar VARCHAR(255),
  name_de VARCHAR(255),
  name_ja VARCHAR(255),
  name_ru VARCHAR(255),
  description_en VARCHAR(2000),
  description_vi VARCHAR(2000),
  description_fr VARCHAR(2000),
  description_ar VARCHAR(2000),
  description_de VARCHAR(2000),
  description_ja VARCHAR(2000),
  description_ru VARCHAR(2000),
  price NUMERIC(19, 2),
  image_url VARCHAR(255),
  release_date DATE,
  category_id BIGINT REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(255),
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255),
  role VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS orders (
  id BIGSERIAL PRIMARY KEY,
  customer_name VARCHAR(255),
  customer_email VARCHAR(255),
  customer_phone VARCHAR(255),
  status VARCHAR(255),
  discount_base NUMERIC(19, 2),
  shipping_address_line VARCHAR(255),
  shipping_city VARCHAR(255),
  shipping_state VARCHAR(255),
  shipping_postal_code VARCHAR(255),
  customer_note VARCHAR(1000),
  created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
  id BIGSERIAL PRIMARY KEY,
  quantity INTEGER,
  unit_price NUMERIC(19, 2),
  order_id BIGINT REFERENCES orders(id),
  product_id BIGINT REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS carts (
  id BIGSERIAL PRIMARY KEY,
  session_key VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS cart_items (
  id BIGSERIAL PRIMARY KEY,
  quantity INTEGER,
  cart_id BIGINT REFERENCES carts(id),
  product_id BIGINT REFERENCES products(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_languages_code ON languages(code);
CREATE UNIQUE INDEX IF NOT EXISTS ux_currencies_code ON currencies(code);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users(email);
CREATE UNIQUE INDEX IF NOT EXISTS ux_carts_session_key ON carts(session_key);

-- Languages (7 core + 20 synthetic)
INSERT INTO languages (code, name, locale_code, rtl, active, default_language)
VALUES
  ('vi', 'Vietnamese', 'vi-VN', FALSE, TRUE, TRUE),
  ('en', 'English', 'en-US', FALSE, TRUE, FALSE),
  ('fr', 'French', 'fr-FR', FALSE, TRUE, FALSE),
  ('ar', 'Arabic', 'ar-SA', TRUE, TRUE, FALSE),
  ('de', 'German', 'de-DE', FALSE, TRUE, FALSE),
  ('ja', 'Japanese', 'ja-JP', FALSE, TRUE, FALSE),
  ('ru', 'Russian', 'ru-RU', FALSE, TRUE, FALSE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO languages (code, name, locale_code, rtl, active, default_language)
SELECT
  'tlang' || LPAD(gs::text, 2, '0'),
  'Test Language ' || gs,
  CASE
    WHEN gs % 7 = 1 THEN 'en-US'
    WHEN gs % 7 = 2 THEN 'vi-VN'
    WHEN gs % 7 = 3 THEN 'fr-FR'
    WHEN gs % 7 = 4 THEN 'ar-SA'
    WHEN gs % 7 = 5 THEN 'de-DE'
    WHEN gs % 7 = 6 THEN 'ja-JP'
    ELSE 'ru-RU'
  END,
  (gs % 7 = 4),
  TRUE,
  FALSE
FROM generate_series(1, 20) AS gs
ON CONFLICT (code) DO NOTHING;

-- Currencies (3 core + 20 synthetic)
INSERT INTO currencies (
  code,
  name,
  symbol,
  locale_code,
  exchange_rate_to_default,
  decimal_digits,
  symbol_position,
  active,
  default_currency
)
VALUES
  ('VND', 'Vietnamese Dong', 'd', 'vi-VN', 1.0000, 0, 'AFTER', TRUE, TRUE),
  ('USD', 'US Dollar', '$', 'en-US', 0.000040, 2, 'BEFORE', TRUE, FALSE),
  ('EUR', 'Euro', 'EUR', 'fr-FR', 0.000037, 2, 'AFTER', TRUE, FALSE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO currencies (
  code,
  name,
  symbol,
  locale_code,
  exchange_rate_to_default,
  decimal_digits,
  symbol_position,
  active,
  default_currency
)
SELECT
  'TC' || LPAD(gs::text, 2, '0'),
  'Test Currency ' || gs,
  '$' || gs,
  CASE
    WHEN gs % 3 = 1 THEN 'en-US'
    WHEN gs % 3 = 2 THEN 'vi-VN'
    ELSE 'fr-FR'
  END,
  (0.50 + gs * 0.10)::numeric(12,4),
  2,
  CASE WHEN gs % 2 = 0 THEN 'BEFORE' ELSE 'AFTER' END,
  TRUE,
  FALSE
FROM generate_series(1, 20) AS gs
ON CONFLICT (code) DO NOTHING;

-- Categories (20)
INSERT INTO categories (name_en, name_vi, name_fr, name_ar, name_de, name_ja, name_ru)
SELECT
  'Seed Category ' || gs,
  'Danh muc thu ' || gs,
  'Categorie test ' || gs,
  'fie tajribiya ' || gs,
  'Testkategorie ' || gs,
  'Tesuto kategori ' || gs,
  'Testovaya kategoriya ' || gs
FROM generate_series(1, 20) AS gs
WHERE NOT EXISTS (
  SELECT 1
  FROM categories c
  WHERE c.name_en = 'Seed Category ' || gs
);

-- Products (30) so storefront always has enough catalog items.
INSERT INTO products (
  name_en,
  name_vi,
  name_fr,
  name_ar,
  name_de,
  name_ja,
  name_ru,
  description_en,
  description_vi,
  description_fr,
  description_ar,
  description_de,
  description_ja,
  description_ru,
  price,
  image_url,
  release_date,
  category_id
)
SELECT
  'Seed Product ' || gs,
  'San pham mau ' || gs,
  'Produit test ' || gs,
  'Muntaj ikhtibari ' || gs,
  'Testprodukt ' || gs,
  'Tesuto shohin ' || gs,
  'Testovyi tovar ' || gs,
  'Seed English description for product ' || gs,
  'Mo ta tieng Viet cho san pham ' || gs,
  'Description francaise pour le produit ' || gs,
  'Wasf tajribi lilmuntaj ' || gs,
  'Testbeschreibung fuer Produkt ' || gs,
  'Tesuto shohin no setsumei ' || gs,
  'Testovoe opisanie tovara ' || gs,
  (100 + gs * 5)::numeric(12,2),
  'https://picsum.photos/seed/seedproduct' || gs || '/640/480',
  CURRENT_DATE - gs,
  (
    SELECT c.id
    FROM categories c
    WHERE c.name_en = 'Seed Category ' || (((gs - 1) % 20) + 1)
    LIMIT 1
  )
FROM generate_series(1, 30) AS gs
WHERE NOT EXISTS (
  SELECT 1
  FROM products p
  WHERE p.name_en = 'Seed Product ' || gs
);

-- Add extra synthetic users for testing lists/history views.
INSERT INTO users (full_name, email, password, role)
SELECT
  'Seed User ' || gs,
  'seeduser' || LPAD(gs::text, 2, '0') || '@example.com',
  '$2a$10$5vP8hM6kQYQdD0m9M9L6d.u9hfhv2vA1fQ5m6o6H9Q9H4mPjW7q7a',
  CASE WHEN gs % 5 = 0 THEN 'ADMIN' ELSE 'CUSTOMER' END
FROM generate_series(1, 20) AS gs
ON CONFLICT (email) DO NOTHING;

-- Clean previous seeded cart/order children to keep script repeatable.
DELETE FROM order_items oi
USING orders o
WHERE oi.order_id = o.id
  AND o.customer_note LIKE 'SEED_TEST_ORDER_%';

DELETE FROM orders
WHERE customer_note LIKE 'SEED_TEST_ORDER_%';

DELETE FROM cart_items ci
USING carts c
WHERE ci.cart_id = c.id
  AND c.session_key LIKE 'seed-cart-%';

DELETE FROM carts
WHERE session_key LIKE 'seed-cart-%';

-- Orders (20)
INSERT INTO orders (
  customer_name,
  customer_email,
  customer_phone,
  status,
  discount_base,
  shipping_address_line,
  shipping_city,
  shipping_state,
  shipping_postal_code,
  customer_note,
  created_at
)
SELECT
  'Seed User ' || gs,
  'seeduser' || LPAD((((gs - 1) % 20) + 1)::text, 2, '0') || '@example.com',
  '090000' || LPAD(gs::text, 4, '0'),
  CASE
    WHEN gs % 4 = 1 THEN 'NEW'
    WHEN gs % 4 = 2 THEN 'PROCESSING'
    WHEN gs % 4 = 3 THEN 'DONE'
    ELSE 'CANCELLED'
  END,
  CASE WHEN gs % 3 = 0 THEN (-1 * gs)::numeric(12,2) ELSE (gs * 0.5)::numeric(12,2) END,
  'Seed Address Line ' || gs,
  'Seed City ' || gs,
  CASE WHEN gs % 2 = 0 THEN 'CA' ELSE 'TX' END,
  LPAD((10000 + gs)::text, 5, '0'),
  'SEED_TEST_ORDER_' || gs,
  NOW() - (gs || ' hours')::interval
FROM generate_series(1, 20) AS gs;

-- Order items (20)
INSERT INTO order_items (quantity, unit_price, order_id, product_id)
SELECT
  ((gs % 4) + 1),
  (50 + gs * 2)::numeric(12,2),
  (
    SELECT o.id
    FROM orders o
    WHERE o.customer_note = 'SEED_TEST_ORDER_' || gs
    LIMIT 1
  ),
  (
    SELECT p.id
    FROM products p
    WHERE p.name_en = 'Seed Product ' || (((gs - 1) % 30) + 1)
    LIMIT 1
  )
FROM generate_series(1, 20) AS gs;

-- Carts (20)
INSERT INTO carts (session_key)
SELECT 'seed-cart-' || LPAD(gs::text, 2, '0')
FROM generate_series(1, 20) AS gs
ON CONFLICT (session_key) DO NOTHING;

-- Cart items (20)
INSERT INTO cart_items (quantity, cart_id, product_id)
SELECT
  ((gs % 3) + 1),
  (
    SELECT c.id
    FROM carts c
    WHERE c.session_key = 'seed-cart-' || LPAD(gs::text, 2, '0')
    LIMIT 1
  ),
  (
    SELECT p.id
    FROM products p
    WHERE p.name_en = 'Seed Product ' || (((gs - 1) % 30) + 1)
    LIMIT 1
  )
FROM generate_series(1, 20) AS gs;

COMMIT;
