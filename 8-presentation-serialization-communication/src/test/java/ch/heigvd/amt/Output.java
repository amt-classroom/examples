package ch.heigvd.amt;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/** Ce qu'un exemple écrit sur la sortie standard, pour le comparer à ce qu'on attend. */
public final class Output {

    private Output() {
    }

    /** Les lignes que l'action écrit sur System.out. */
    public static List<String> printed(Runnable action) {
        PrintStream original = System.out;
        var buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString().lines().toList();
    }
}
