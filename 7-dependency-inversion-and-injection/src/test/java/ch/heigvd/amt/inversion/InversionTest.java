package ch.heigvd.amt.inversion;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Le couplage faible : le haut niveau accepte tout fournisseur de Logger. */
class InversionTest {

    @Test
    void mainChoosesTheConsole() {
        assertEquals(List.of("Performing an operation"), printed(() -> Main.main(new String[0])));
    }

    @Test
    void aTestChoosesAnotherLogger() {
        List<String> logged = new ArrayList<>();
        var app = new Application(logged::add);

        app.performOperation();

        assertEquals(List.of("Performing an operation"), logged);
    }
}
