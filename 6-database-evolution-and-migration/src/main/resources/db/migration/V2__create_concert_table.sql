CREATE TABLE concert (
    id serial PRIMARY KEY,
    stage_id integer NOT NULL REFERENCES stage,
    artist_name text NOT NULL,
    starts_at timestamp NOT NULL
);
