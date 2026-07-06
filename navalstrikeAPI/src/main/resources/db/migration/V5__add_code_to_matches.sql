ALTER TABLE matches ADD COLUMN code VARCHAR(6) NOT NULL DEFAULT '';


UPDATE matches SET code = UPPER(SUBSTRING(REPLACE(CAST(gen_random_uuid() AS VARCHAR), '-', ''), 1, 6)) WHERE code = '';


ALTER TABLE matches ADD CONSTRAINT uq_matches_code UNIQUE (code);
