package ch.heigvd.amt.inversion.direct;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/** Le couplage fort : impossible de tester le haut niveau sans exécuter le bas niveau. */
class DirectDependencyTest {

    @Test
    void theApplicationAlwaysWritesToTheConsole() {
        var app = new Application();

        assertEquals(List.of("Performing an operation"), printed(app::performOperation));
    }
}
