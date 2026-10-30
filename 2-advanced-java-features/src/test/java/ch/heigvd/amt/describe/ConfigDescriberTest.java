package ch.heigvd.amt.describe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Config_Describer, généré à la compilation par DescriberProcessor à partir de @Describe et @Hidden. */
class ConfigDescriberTest {

    @Test
    void theGeneratedDescriberMasksTheHiddenField() {
        assertEquals("Config[host=localhost, token=***]", Config_Describer.describe(new Config()));
    }
}
