package ch.heigvd.amt.serialization;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.jupiter.api.Test;

/** La sérialisation Java : l'aller-retour, le champ transient, puis le filtre. */
class SerializationTest {

    @Test
    void theTransientFieldDoesNotTravel() {
        assertEquals(
                List.of("106 bytes", "Ticket{holder='Ada', quantity=2, accessCode=null}"),
                printed(() -> {
                    try {
                        Main.main(new String[0]);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }));
    }

    @Test
    void theFilterAcceptsATicket() throws Exception {
        var ticket = Filter.read(bytes(new Ticket("Ada", 2, "X7K2")));

        assertEquals("Ticket{holder='Ada', quantity=2, accessCode=null}", ticket.toString());
    }

    @Test
    void theFilterRejectsAClassOutsideTheList() throws Exception {
        byte[] bytes = bytes(new javax.swing.JButton());

        var e = assertThrows(InvalidClassException.class, () -> Filter.read(bytes));
        System.out.println(e.getMessage());

        assertEquals("filter status: REJECTED", e.getMessage());
    }

    static byte[] bytes(Object object) throws IOException {
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(object);
        }
        return bytes.toByteArray();
    }
}
