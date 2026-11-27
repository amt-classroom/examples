package ch.heigvd.amt.serialization.external;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

/** La classe écrit ses champs elle-même : le flux ne décrit plus leurs noms ni leurs types. */
class ExternalizableTest {

    @Test
    void theTicketWritesAndReadsItsOwnFields() throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(new Ticket("Ada", 2));
        }
        System.out.println(bytes.size() + " bytes");

        try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            assertEquals("Ticket{holder='Ada', quantity=2}", in.readObject().toString());
        }
        assertEquals(76, bytes.size());
    }
}
