package ch.heigvd.amt.cdi.interceptor;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/** Le conteneur place l'interceptor autour de l'appel. */
@HelidonTest
@DisableDiscovery
@AddBean(value = LoggedInterceptor.class, scope = Dependent.class)
@AddBean(ReservationService.class)
class InterceptorTest {

    @Inject
    ReservationService service;

    @Test
    void theInterceptorSurroundsTheCall() {
        assertEquals(List.of(
                "Before reserve",
                "2 tickets reserved",
                "After reserve"), printed(() -> service.reserve("ada@example.com", 2)));
    }
}
