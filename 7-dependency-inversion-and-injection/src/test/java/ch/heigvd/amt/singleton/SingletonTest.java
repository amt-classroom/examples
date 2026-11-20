package ch.heigvd.amt.singleton;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/** Le singleton écrit à la main : la classe ne laisse exister qu'une instance. */
class SingletonTest {

    @Test
    void everyCallerGetsTheSameInstance() {
        assertSame(ConsoleLogger.getInstance(), ConsoleLogger.getInstance());
    }
}
