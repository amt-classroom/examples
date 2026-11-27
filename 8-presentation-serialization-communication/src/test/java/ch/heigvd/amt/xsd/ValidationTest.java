package ch.heigvd.amt.xsd;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Le schéma dit ce qu'un programme doit contenir : un document qui s'en écarte est refusé. */
class ValidationTest {

    @BeforeAll
    static void messagesInEnglish() {
        Locale.setDefault(Locale.ENGLISH); // le validateur traduit ses messages
    }

    @Test
    void aProgrammeThatFollowsTheSchemaIsValid() {
        assertEquals(List.of("Valid"), validate("""
                <programme year="2027">
                    <concert id="1">
                        <artist>Zaho de Sagazan</artist>
                        <stage>Grande scène</stage>
                    </concert>
                </programme>
                """));
    }

    @Test
    void aProgrammeWithoutItsYearIsInvalid() {
        assertEquals(List.of("Invalid: cvc-complex-type.4: "
                + "Attribute 'year' must appear on element 'programme'."), validate("""
                <programme>
                    <concert id="1">
                        <artist>Zaho de Sagazan</artist>
                        <stage>Grande scène</stage>
                    </concert>
                </programme>
                """));
    }

    @Test
    void aConcertWithoutItsStageIsInvalid() {
        assertEquals(List.of("Invalid: cvc-complex-type.2.4.b: "
                + "The content of element 'concert' is not complete. "
                + "One of '{stage}' is expected."), validate("""
                <programme year="2027">
                    <concert id="1">
                        <artist>Zaho de Sagazan</artist>
                    </concert>
                </programme>
                """));
    }

    private static List<String> validate(String xml) {
        return printed(() -> {
            try {
                Validation.validate(xml);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
