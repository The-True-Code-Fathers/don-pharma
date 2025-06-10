-- SQL INSERT script for the 'banana_and_cream_cheese_2025' table

-- Start a transaction for atomicity
BEGIN;

-- IMPORTANT:
-- The 'passwordHash' values below are *example* BCrypt hashes.
-- For actual user creation in your application, you **MUST** use
-- your `PasswordUtil.hashPassword("your_plain_text_password")` method
-- to generate secure hashes before inserting them into the database.
-- Never store plain-text passwords or weak hashes.

-- Insert an admin user
INSERT INTO banana_and_cream_cheese_2025 (username, passwordHash, role)
VALUES (
           'admin',
           '$2a$12$K1dJ.Q1Z.Y8bZ4.P2mZ3yO.C4x.R6x.F8x.J9x.K1x.L2x.M3x.N4x.O5x.P6x.Q7x.R8x.S9x.T0x.U1x.V2x', -- Example hash for "adminpass"
           'ADMIN'
       );

-- Insert a regular user
INSERT INTO banana_and_cream_cheese_2025 (username, passwordHash, role)
VALUES (
           'user',
           '$2a$12$E0eJ.A3F.G5cK7L9m.P1t.V3y.Z5b.X7c.D9e.F1g.H3j.I5l.M7n.O9p.Q1r.S3t.U5w.W7y.Z9a.B2c.D4e', -- Example hash for "password"
           'USER'
       );

-- Commit the transaction if all inserts are successful
COMMIT;