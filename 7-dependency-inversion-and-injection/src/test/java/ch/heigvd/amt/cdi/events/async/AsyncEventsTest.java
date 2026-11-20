package ch.heigvd.amt.cdi.events.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/** fireAsync appelle les observers sur un autre thread. */
@HelidonTest
@DisableDiscovery
@AddBean(ReservationService.class)
@AddBean(Statistics.class)
class AsyncEventsTest {

    @Inject
    ReservationService service;

    @Inject
    Statistics statistics;

    @Test
    void theObserverRunsOnAnotherThread() throws Exception {
        // Le test attend la fin des observers ; l'émetteur, lui, a déjà repris la main.
        service.reserve("ada@example.com", 2).toCompletableFuture().get();

        assertEquals(2, statistics.tickets());
        assertNotEquals(Thread.currentThread().getName(), statistics.thread());
    }
}
