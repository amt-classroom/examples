package ch.heigvd.amt.doubles;

import java.util.ArrayList;
import java.util.List;

import ch.heigvd.amt.festival.Mailer;

/** Un fake : une vraie implémentation, simplifiée, qui garde tout en mémoire. */
class FakeMailer implements Mailer {
    private final List<String> sent = new ArrayList<>();

    public void send(String to, String message) {
        sent.add(to + ": " + message);
    }

    List<String> sent() {
        return sent;
    }
}
