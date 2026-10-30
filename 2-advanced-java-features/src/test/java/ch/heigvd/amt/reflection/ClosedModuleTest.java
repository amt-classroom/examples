package ch.heigvd.amt.reflection;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InaccessibleObjectException;

import org.junit.jupiter.api.Test;

/** Depuis le JDK 17, setAccessible ne peut plus ouvrir un module que personne n'a déclaré ouvert. */
class ClosedModuleTest {

    @Test
    void setAccessibleCannotOpenAnUnopenedJdkModule() throws NoSuchFieldException {
        var value = String.class.getDeclaredField("value"); // un champ interne du JDK
        assertThrows(InaccessibleObjectException.class, () -> value.setAccessible(true));
    }
}
