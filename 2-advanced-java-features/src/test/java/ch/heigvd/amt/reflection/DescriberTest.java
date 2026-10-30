package ch.heigvd.amt.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DescriberTest {

    @Test
    void describesThePrivateFieldsOfAnyObject() throws IllegalAccessException {
        assertEquals("Point[x=3, y=4]", Describer.describe(new Point(3, 4)));
    }
}
