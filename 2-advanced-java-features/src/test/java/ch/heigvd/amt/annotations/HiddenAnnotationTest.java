package ch.heigvd.amt.annotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HiddenAnnotationTest {

    @Test
    void anOrdinaryFieldIsNotAnnotated() throws NoSuchFieldException {
        assertFalse(Config.class.getDeclaredField("host").isAnnotationPresent(Hidden.class));
    }

    @Test
    void aHiddenFieldIsAnnotated() throws NoSuchFieldException {
        assertTrue(Config.class.getDeclaredField("token").isAnnotationPresent(Hidden.class));
    }
}
