package ch.heigvd.amt.jpa;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;

/** Le côté propriétaire d'une relation : lui seul porte la clé étrangère, et l'écrit. */
class OwningSideTest extends TicketingTest {

    @Test
    void theManyToOneSideCarriesTheForeignKey() {
        var ddl = Postgres.ddl("ticketing");

        assertTrue(ddl.contains("foreign key (stage_id) references Stage"), ddl);
        assertFalse(ddl.contains("Stage_Concert"), ddl); // mappedBy : ni colonne ni table pour la liste
    }

    /** Le piège : la liste est le côté inverse, la remplir n'écrit rien. */
    @Test
    void onlyTheOwningSideIsWritten() {
        var stage = new Stage("Le Club");
        var concert = new Concert("Menhirs", at(19, 30));
        Postgres.insert(factory, stage, concert);

        em.getTransaction().begin();
        Concert managed = em.find(Concert.class, concert.getId());
        em.find(Stage.class, stage.getId()).getConcerts().add(managed); // la liste seule, pas managed.stage
        em.getTransaction().commit();

        assertNull(stored(Concert.class, concert.getId()).getStage());
    }
}
