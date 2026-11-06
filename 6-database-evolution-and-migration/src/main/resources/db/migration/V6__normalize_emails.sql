CREATE OR REPLACE PROCEDURE normalize_emails(batch_size integer)
LANGUAGE plpgsql AS $$
DECLARE
    max_id integer := (SELECT coalesce(max(id), 0) FROM reservation);
BEGIN
    FOR first_id IN 1..max_id BY batch_size LOOP
        UPDATE reservation SET email = lower(email)
        WHERE id >= first_id AND id < first_id + batch_size;
        COMMIT;
    END LOOP;
END;
$$;

CALL normalize_emails(10000);
