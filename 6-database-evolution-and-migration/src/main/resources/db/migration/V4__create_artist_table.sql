CREATE TABLE artist (
    id serial PRIMARY KEY,
    name text NOT NULL UNIQUE
);
INSERT INTO artist (name)
    SELECT DISTINCT artist_name FROM concert;

ALTER TABLE concert ADD COLUMN artist_id integer REFERENCES artist;
UPDATE concert SET artist_id = artist.id
    FROM artist WHERE artist.name = concert.artist_name;

ALTER TABLE concert ALTER COLUMN artist_id SET NOT NULL;
ALTER TABLE concert DROP COLUMN artist_name;
