package ch.heigvd.amt.defaults;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/** Une relation laissée aux défauts : un @OneToMany seul, et le schéma qu'il produit. */
class OneToManyDefaultsTest {

    @Test
    void aOneToManyAloneGetsAJoinTable() {
        var ddl = Postgres.ddl("defaults");

        // PostgreSQL passe les noms en minuscules : stage_concert, stage_id, concerts_id.
        assertTrue(ddl.contains("create table Stage_Concert (Stage_id bigint not null, concerts_id bigint not null unique)"), ddl);
    }
}
