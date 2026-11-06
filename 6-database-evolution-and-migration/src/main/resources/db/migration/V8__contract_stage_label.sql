DROP TRIGGER stage_sync_label ON stage;
DROP FUNCTION sync_stage_label();

ALTER TABLE stage ALTER COLUMN label SET NOT NULL;
ALTER TABLE stage DROP COLUMN name;
