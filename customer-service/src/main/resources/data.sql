-- Ensure the 'customer' table exists
CREATE TABLE IF NOT EXISTS customer
(
    id              UUID PRIMARY KEY,
    name            VARCHAR(255)        NOT NULL,
    email           VARCHAR(255) UNIQUE NOT NULL,
    address         VARCHAR(255)        NOT NULL,
    date_of_birth   DATE                NOT NULL,
    registered_date DATE                NOT NULL
);

-- Insert 5 test customers
INSERT INTO customer (id, name, email, address, date_of_birth, registered_date)
SELECT 'b7ca288e-cd8f-4ab7-b32e-22556ba1e948',
       'Customer 1',
       'customer1@gmail.com',
       'Customer1 Street',
       '1990-01-01',
       '2026-01-01'
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE id = 'b7ca288e-cd8f-4ab7-b32e-22556ba1e948');

INSERT INTO customer (id, name, email, address, date_of_birth, registered_date)
SELECT 'b37f1eb5-3e27-4258-97ec-88e5736812cc',
       'Customer 2',
       'customer2@gmail.com',
       'Customer2 Street',
       '1990-01-02',
       '2026-01-02'
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE id = 'b37f1eb5-3e27-4258-97ec-88e5736812cc');

INSERT INTO customer (id, name, email, address, date_of_birth, registered_date)
SELECT '138828ef-1017-4f22-9036-b0380da94e1a',
       'Customer 3',
       'customer3@gmail.com',
       'Customer3 Street',
       '1990-01-03',
       '2026-01-03'
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE id = '138828ef-1017-4f22-9036-b0380da94e1a');

INSERT INTO customer (id, name, email, address, date_of_birth, registered_date)
SELECT '0efb715a-d245-4cea-be7a-07da8c893946',
       'Customer 4',
       'customer4@gmail.com',
       'Customer4 Street',
       '1990-01-04',
       '2026-01-04'
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE id = '0efb715a-d245-4cea-be7a-07da8c893946');

INSERT INTO customer (id, name, email, address, date_of_birth, registered_date)
SELECT 'e51eea98-fc4c-42fa-ab93-9d9904c81abe',
       'Customer 5',
       'customer5@gmail.com',
       'Customer5 Street',
       '1990-01-05',
       '2026-01-05'
WHERE NOT EXISTS (SELECT 1 FROM customer WHERE id = 'e51eea98-fc4c-42fa-ab93-9d9904c81abe');