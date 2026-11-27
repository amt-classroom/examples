package ch.heigvd.amt.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InvalidClassException;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.junit5.HelidonTest;

/** Dans Helidon, le même programme échoue : le serveur refuse par défaut toute désérialisation Java. */
@HelidonTest
class HelidonFilterTest {

    @Test
    void helidonRejectsJavaDeserializationByDefault() {
        var e = assertThrows(InvalidClassException.class, () -> Main.main(new String[0]));

        assertEquals("filter status: REJECTED", e.getMessage());
    }
}
