-- Ensure the 'users' table exists
CREATE TABLE IF NOT EXISTS "users" (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Insert the user if no existing user with the same id or email exists
INSERT INTO "users" (id, email, password, role)
SELECT 'd6275854-efa8-4f74-b5a4-1cce63c43a61', 'test@gmail.com',
       '$2b$12$7hoRZfJrRKD2nIm2vHLs7OBETy.LWenXXMLKf99W8M4PUwO6KB7fu', 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1
    FROM "users"
    WHERE id = 'd6275854-efa8-4f74-b5a4-1cce63c43a61'
       OR email = 'test@gmail.com'
);