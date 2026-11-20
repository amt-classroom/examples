package ch.heigvd.amt.control;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider;
import org.junit.jupiter.api.Test;

import ch.heigvd.amt.injection.container.Application;
import ch.heigvd.amt.injection.container.ConsoleLogger;
import io.helidon.microprofile.server.JaxRsCdiExtension;
import io.helidon.microprofile.server.ServerCdiExtension;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.AddExtension;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;

/** Le test n'appelle pas perform : il envoie une requête HTTP, et le conteneur l'appelle. */
@HelidonTest
@DisableDiscovery
@AddExtension(ServerCdiExtension.class)
@AddExtension(JaxRsCdiExtension.class)
@AddExtension(CdiComponentProvider.class)
@AddBean(OperationResource.class)
@AddBean(Application.class)
@AddBean(ConsoleLogger.class)
class InversionOfControlTest {

    @Inject
    WebTarget target;

    @Test
    void theContainerCreatesTheResourceAndCallsIt() {
        assertEquals(List.of("Performing an operation"),
                printed(() -> target.path("/operations").request().post(Entity.text("")).close()));
    }
}
