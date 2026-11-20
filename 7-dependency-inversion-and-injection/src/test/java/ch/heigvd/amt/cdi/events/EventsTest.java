package ch.heigvd.amt.cdi.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/** fire appelle les observateurs et attend leur fin. */
@HelidonTest
@DisableDiscovery
@AddBean(ReservationService.class)
@AddBean(Statistics.class)
class EventsTest {

    @Inject
    ReservationService service;

    @Inject
    Statistics statistics;

    @Test
    void theObserverHasRunWhenFireReturns() {
        service.reserve("ada@example.com", 2);

        assertEquals(2, statistics.tickets());
    }
}
