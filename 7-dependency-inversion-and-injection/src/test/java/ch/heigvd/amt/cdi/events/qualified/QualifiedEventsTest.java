package ch.heigvd.amt.cdi.events.qualified;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.festival.Reservation;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;

/** Un qualifier trie les events d'un même type. */
@HelidonTest
@DisableDiscovery
@AddBean(ReservationService.class)
@AddBean(Journal.class)
class QualifiedEventsTest {

    @Inject
    ReservationService service;

    @Inject
    Journal journal;

    @Test
    void theQualifiedObserverOnlyGetsQualifiedEvents() {
        service.reserve(new Reservation("ada@example.com", 2));
        service.cancel(new Reservation("bob@example.com", 1));

        // L'observer sans qualifier a reçu les deux events. L'ordre entre deux
        // observers d'un même event n'est pas garanti.
        assertEquals(List.of("any ada@example.com", "any bob@example.com", "cancelled bob@example.com"),
                journal.entries().stream().sorted().toList());
    }
}
