package ch.heigvd.amt.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Output;
import ch.heigvd.amt.Postgres;

/** Écrire sans appeler save : le contexte de persistance écrit ce qui a changé, au flush. */
class WritingTest extends TicketingTest {

    private ConcertService service;
    private Concert concert;

    @BeforeEach
    void inject() {
        service = new ConcertService();
        service.em = em; // ce que ferait @PersistenceContext
        concert = new Concert("Pluie d'Acier", at(17, 0));
        Postgres.insert(factory, concert);
    }

    @Test
    void rescheduleWritesTheChangeAtCommit() throws Exception {
        em.getTransaction().begin(); // ce que ferait @Transactional
        var duringTheCall = Output.capture(() -> service.reschedule(concert.getId(), at(18, 0)));
        var atCommit = Output.capture(() -> em.getTransaction().commit());

        assertLinesMatch(List.of("Hibernate: select .* from Concert c1_0 .*"), duringTheCall); // le find seul
        // Toutes les colonnes, pas seulement starts_at.
        assertEquals(List.of("Hibernate: update Concert set artist=?,stage_id=?,starts_at=? where id=?"), atCommit);
        assertEquals(at(18, 0), stored(Concert.class, concert.getId()).getStartsAt());
    }

    @Test
    void cancelDeletesAtFlush() throws Exception {
        em.getTransaction().begin();
        var duringTheCall = Output.capture(() -> service.cancel(concert.getId()));
        var atCommit = Output.capture(() -> em.getTransaction().commit());

        assertLinesMatch(List.of("Hibernate: select .* from Concert c1_0 .*"), duringTheCall);
        assertEquals(List.of("Hibernate: delete from Concert where id=?"), atCommit);
    }

    @Test
    void persistOnAManagedEntityIsIgnored() throws Exception {
        em.getTransaction().begin();
        Concert managed = em.find(Concert.class, concert.getId());

        var sql = Output.capture(() -> {
            em.persist(managed);
            em.getTransaction().commit();
        });

        assertEquals(List.of(), sql); // ni INSERT ni UPDATE
    }

    @Test
    void flushWritesBeforeTheCommit() throws Exception {
        em.getTransaction().begin();
        em.find(Concert.class, concert.getId()).setStartsAt(at(18, 0));

        var atFlush = Output.capture(() -> em.flush());
        var atCommit = Output.capture(() -> em.getTransaction().commit());

        assertLinesMatch(List.of("Hibernate: update Concert .*"), atFlush);
        assertEquals(List.of(), atCommit); // plus rien à écrire
    }

    @Test
    void aQueryFlushesTheChangesItDependsOn() throws Exception {
        em.getTransaction().begin();
        em.find(Concert.class, concert.getId()).setStartsAt(at(18, 0));

        var stages = Output.capture(() -> em.createQuery("SELECT s FROM Stage s", Stage.class).getResultList());
        var concerts = Output.capture(() -> em.createQuery("SELECT c FROM Concert c", Concert.class).getResultList());

        // La table stage ne dépend pas du changement : il attend. La table concert, si.
        assertEquals(List.of("Hibernate: select s1_0.id,s1_0.name from Stage s1_0"), stages);
        assertLinesMatch(List.of("Hibernate: update Concert .*", "Hibernate: select .* from Concert c1_0"), concerts);
    }
}
