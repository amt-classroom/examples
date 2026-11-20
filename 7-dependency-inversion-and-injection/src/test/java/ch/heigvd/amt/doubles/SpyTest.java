package ch.heigvd.amt.doubles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Un espion enveloppe un vrai objet : il exécute les vraies méthodes et enregistre les appels. */
class SpyTest {

    @Test
    void aSpyKeepsTheRealMethodsUnlessToldOtherwise() {
        List<String> list = spy(new ArrayList<>());

        list.add("one");                  // la vraie méthode
        list.add("two");
        assertEquals(2, list.size());

        doReturn(100).when(list).size();  // méthode remplacée
        assertEquals(100, list.size());

        assertEquals("one", list.get(0)); // le reste est intact
        verify(list).add("one");
    }
}
