package ch.heigvd.amt.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;

/** Relit des bytes en n'acceptant que les classes d'une liste. */
public class Filter {
    public static Object read(byte[] bytes) throws IOException, ClassNotFoundException {
        var filter = ObjectInputFilter.Config
                .createFilter("ch.heigvd.amt.**;java.base/*;!*");

        try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            in.setObjectInputFilter(filter);
            return in.readObject();
        }
    }
}
