ALTER TABLE stage ADD COLUMN label text;
UPDATE stage SET label = name;

CREATE FUNCTION sync_stage_label() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.name IS DISTINCT FROM OLD.name THEN
        NEW.label := NEW.name;
    ELSIF NEW.label IS DISTINCT FROM OLD.label THEN
        NEW.name := NEW.label;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER stage_sync_label
    BEFORE INSERT OR UPDATE ON stage
    FOR EACH ROW
    EXECUTE FUNCTION sync_stage_label();
