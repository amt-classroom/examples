package ch.heigvd.amt.bulk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Postgres;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * 100 000 concerts rendus persistants avec Hibernate : sans lot, par lots de 50 en vidant le
 * contexte, puis avec une clé IDENTITY, qui empêche les lots.
 */
class HibernateImportTest {

    static final LocalDateTime OPENING = LocalDateTime.of(2027, 7, 16, 14, 0);

    @Test
    void withoutBatching() {
        try (EntityManagerFactory factory = Postgres.entityManagerFactory("bulk", Map.of());
                EntityManager em = factory.createEntityManager()) {
            Statistics statistics = statistics(factory);
            List<Concert> concerts = concerts();
            long start = System.nanoTime();
            em.getTransaction().begin();
            for (Concert concert : concerts) {
                em.persist(concert);
            }
            long managed = em.unwrap(Session.class).getStatistics().getEntityCount();
            em.getTransaction().commit();
            report("persist, no batching", start, statistics);

            assertEquals(100_000, managed); // les 100 000 concerts restent dans le contexte
        }
    }

    @Test
    void batchesOfFifty() {
        try (EntityManagerFactory factory = Postgres.entityManagerFactory("bulk", Map.of("hibernate.jdbc.batch_size", 50));
                EntityManager em = factory.createEntityManager()) {
            Statistics statistics = statistics(factory);
            List<Concert> concerts = concerts();
            long start = System.nanoTime();
            em.getTransaction().begin();
            Imports.persistAll(em, concerts);
            long managed = em.unwrap(Session.class).getStatistics().getEntityCount();
            em.getTransaction().commit();
            report("persist, batch_size 50, flush and clear", start, statistics);

            assertEquals(0, managed); // vidé au dernier lot
            assertTrue(statistics.getPrepareStatementCount() < 10_000); // un INSERT par lot
        }
    }

    @Test
    void identityPreventsBatching() {
        try (EntityManagerFactory factory = Postgres.entityManagerFactory("bulk", Map.of("hibernate.jdbc.batch_size", 50));
                EntityManager em = factory.createEntityManager()) {
            Statistics statistics = statistics(factory);
            List<IdentityConcert> concerts = IntStream.range(0, 100_000)
                .mapToObj(i -> new IdentityConcert("Artist " + i, OPENING.plusMinutes(i)))
                .toList();
            long start = System.nanoTime();
            em.getTransaction().begin();
            Imports.persistAll(em, concerts);
            em.getTransaction().commit();
            report("persist IDENTITY, batch_size 50, flush and clear", start, statistics);

            assertEquals(100_000, statistics.getPrepareStatementCount()); // un INSERT par concert
        }
    }

    /** De nouveaux concerts à chaque test : un concert déjà persistant ne peut pas l'être une seconde fois. */
    private static List<Concert> concerts() {
        return IntStream.range(0, 100_000)
            .mapToObj(i -> new Concert("Artist " + i, OPENING.plusMinutes(i)))
            .toList();
    }

    /** Les statistiques de Hibernate, remises à zéro après la création du schéma. */
    private static Statistics statistics(EntityManagerFactory factory) {
        Statistics statistics = factory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        return statistics;
    }

    private static void report(String what, long start, Statistics statistics) {
        System.out.printf("%-55s %,8d ms, %,7d statements prepared%n", what,
            (System.nanoTime() - start) / 1_000_000, statistics.getPrepareStatementCount());
    }
}
