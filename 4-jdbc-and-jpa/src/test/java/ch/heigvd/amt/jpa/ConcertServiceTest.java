package ch.heigvd.amt.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Output;
import ch.heigvd.amt.Postgres;

/**
 * L'EntityManager dans un service. Aucun conteneur ne tourne ici, le test en tient le
 * rôle : il injecte l'EntityManager et ouvre la transaction que
 * {@code @Transactional} ouvrirait.
 */
class ConcertServiceTest extends TicketingTest {

    private ConcertService service;

    @BeforeEach
    void inject() {
        service = new ConcertService();
        service.em = em; // ce que ferait @PersistenceContext
    }

    @Test
    void persistInsertsAtCommitAtTheLatest() throws Exception {
        var concert = new Concert("Menhirs", at(19, 30));

        em.getTransaction().begin();
        var duringTheCall = Output.capture(() -> service.schedule(concert));
        var atCommit = Output.capture(() -> em.getTransaction().commit());

        // persist ne demande à la base qu'une clé, à la séquence ; la ligne attend le commit.
        assertTrue(duringTheCall.stream().noneMatch(sql -> sql.contains("insert")), duringTheCall::toString);
        assertLinesMatch(List.of("Hibernate: insert into Concert .*"), atCommit);
    }

    @Test
    void findSelectsByPrimaryKey() throws Exception {
        var concert = new Concert("Vieux Port", at(18, 0));
        Postgres.insert(factory, concert);

        var sql = Output.capture(() -> service.find(concert.getId()));

        assertLinesMatch(List.of("Hibernate: select .* from Concert c1_0 .*where c1_0.id=\\?"), sql);
    }

    @Test
    void theContextHoldsOneInstancePerKey() throws Exception {
        var concert = new Concert("Thièle Noise", at(15, 30));
        Postgres.insert(factory, concert);

        Concert first = service.find(concert.getId());
        var sql = Output.capture(() -> assertSame(first, service.find(concert.getId())));

        assertEquals(List.of(), sql); // le second find ne va pas en base
    }
}
