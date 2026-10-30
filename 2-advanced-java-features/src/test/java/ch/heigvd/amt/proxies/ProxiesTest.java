package ch.heigvd.amt.proxies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Le proxy journalise ce qui lui arrive de l'extérieur, rien de plus. */
class ProxiesTest {

    @Test
    void loggedInterceptsAndForwardsTheCall() {
        var calculator = Proxies.logged(Calculator.class, new BasicCalculator());
        var output = capture(() -> assertEquals(5, calculator.add(2, 3)));
        assertEquals(List.of("-> add[2, 3]"), output);
    }

    @Test
    void anInternalCallEscapesTheProxy() {
        var calculator = Proxies.logged(Calculator.class, new BasicCalculator());
        var output = capture(() -> assertEquals(6, calculator.addAll(List.of(1, 2, 3))));
        // addAll est journalisée ; les add() qu'elle appelle en interne (this.add()) ne le sont pas.
        assertEquals(List.of("-> addAll[[1, 2, 3]]"), output);
    }

    @Test
    void aProxyIsOnlyTheInterfaceItImplements() {
        Calculator calculator = Proxies.logged(Calculator.class, new BasicCalculator());
        assertThrows(ClassCastException.class, () -> {
            var cast = (BasicCalculator) calculator;
        });
    }

    /** Capture ce que le runnable écrit sur System.out, une ligne par élément. */
    private static List<String> capture(Runnable runnable) {
        var original = System.out;
        var buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            runnable.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString().lines().toList();
    }
}
