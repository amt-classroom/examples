package ch.heigvd.amt.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** Écrit un billet en bytes, puis le relit : le champ transient ne fait pas le voyage. */
public class Main {
    public static void main(String[] args) throws Exception {
        var ticket = new Ticket("Ada", 2, "X7K2");

        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) {
            out.writeObject(ticket);
        }
        System.out.println(bytes.size() + " bytes");

        try (var in = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            var copy = (Ticket) in.readObject();
            System.out.println(copy);
        }
    }
}
