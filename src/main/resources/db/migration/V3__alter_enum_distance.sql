ALTER TABLE users
    MODIFY distance ENUM('LEVEL1', 'LEVEL2', 'LEVEL3', 'LEVEL4') NULL;

UPDATE users SET distance = 'LEVEL1' WHERE distance = '1';
UPDATE users SET distance = 'LEVEL2' WHERE distance = '2';
UPDATE users SET distance = 'LEVEL3' WHERE distance = '3';
UPDATE users SET distance = 'LEVEL4' WHERE distance = '4';
