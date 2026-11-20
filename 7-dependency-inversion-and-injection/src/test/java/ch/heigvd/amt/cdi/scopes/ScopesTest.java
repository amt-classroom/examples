package ch.heigvd.amt.cdi.scopes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.Container;
import jakarta.enterprise.context.control.RequestContextController;

/** La durée de vie des instances selon leur scope, et le proxy que le conteneur injecte. */
class ScopesTest {

    @Test
    void aDependentBeanIsCreatedForEachInjectionPoint() {
        try (var container = Container.start(ReservationService.class, CurrentBuyer.class, Receipt.class)) {
            var service = container.select(ReservationService.class).get();

            assertNotSame(service.receipt(), service.copy());
        }
    }

    @Test
    void theContainerInjectsAProxy() {
        try (var container = Container.start(ReservationService.class, CurrentBuyer.class, Receipt.class)) {
            CurrentBuyer buyer = container.select(ReservationService.class).get().buyer();

            // Une sous-classe de CurrentBuyer générée par le conteneur, pas CurrentBuyer.
            assertNotSame(CurrentBuyer.class, buyer.getClass());
        }
    }

    @Test
    void eachRequestHasItsOwnBuyer() {
        try (var container = Container.start(ReservationService.class, CurrentBuyer.class, Receipt.class)) {
            // Le même proxy sert toutes les requêtes.
            CurrentBuyer buyer = container.select(ReservationService.class).get().buyer();
            var request = container.select(RequestContextController.class).get();

            request.activate();
            buyer.identify("ada@example.com");
            assertEquals("ada@example.com", buyer.email());
            request.deactivate();

            request.activate();
            assertNull(buyer.email()); // une autre requête, une autre instance
            request.deactivate();
        }
    }
}
