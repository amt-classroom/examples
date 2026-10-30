package ch.heigvd.amt;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Ce qu'un bloc écrit sur System.out, une ligne par élément : les println des exemples, et le
 * SQL que Hibernate y écrit quand hibernate.show_sql est actif.
 */
public final class Output {

    /** Un bloc de test, qui peut lever les exceptions vérifiées de JDBC. */
    @FunctionalInterface
    public interface Block {
        void run() throws Exception;
    }

    private Output() {
    }

    public static List<String> capture(Block block) throws Exception {
        var original = System.out;
        var buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            block.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8).lines().toList();
    }
}
