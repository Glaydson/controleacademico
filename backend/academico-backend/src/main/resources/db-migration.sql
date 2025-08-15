-- Migration script to fix the pessoa table schema
-- This script removes the matricula column from pessoa table
-- The matricula column already exists correctly in the aluno table

-- EXAMPLE: How to alter/update contents of a field in a table
-- Basic UPDATE syntax:
-- UPDATE table_name SET column_name = new_value WHERE condition;

-- Examples of updating field contents:

-- 1. Update a specific record by ID
-- UPDATE pessoa SET nome = 'New Name' WHERE id = 1;

-- 2. Update multiple records with a condition
-- UPDATE pessoa SET keycloak_id = CONCAT('user_', id) WHERE keycloak_id IS NULL;

-- 3. Update based on another table (JOIN update)
-- UPDATE aluno SET matricula = CONCAT('ALU', LPAD(id::text, 6, '0'))
-- FROM pessoa p WHERE aluno.id = p.id AND aluno.matricula IS NULL;

-- 4. Update using string functions
-- UPDATE pessoa SET nome = UPPER(nome) WHERE tipo_pessoa = 'PROFESSOR';

-- 5. Conditional update with CASE
-- UPDATE pessoa SET nome = CASE
--     WHEN tipo_pessoa = 'ALUNO' THEN CONCAT('Estudante: ', nome)
--     WHEN tipo_pessoa = 'PROFESSOR' THEN CONCAT('Prof. ', nome)
--     ELSE nome
-- END;

-- 6. Update with calculations
-- UPDATE aluno SET matricula = CONCAT('2024', LPAD(id::text, 4, '0'));

-- Remove the redundant matricula column from pessoa table
-- This column should only exist in the aluno table as per the JPA inheritance model
ALTER TABLE pessoa DROP COLUMN IF EXISTS matricula;

-- Verify the changes
-- You can uncomment these lines to check the results:
-- SELECT 'pessoa table structure:' as info;
-- \d pessoa
-- SELECT 'aluno table structure:' as info;
-- \d aluno
