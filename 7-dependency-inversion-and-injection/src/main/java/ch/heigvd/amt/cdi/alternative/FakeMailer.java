package ch.heigvd.amt.cdi.alternative;

import java.util.ArrayList;
import java.util.List;

import ch.heigvd.amt.festival.Mailer;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

/**
 * Une alternative remplace un autre bean du même type, sans toucher aux points d'injection.
 * {@code @Priority} l'active.
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class FakeMailer implements Mailer {
    private final List<String> sent = new ArrayList<>();

    public void send(String to, String message) {
        sent.add(to + ": " + message);
    }

    public List<String> sent() {
        return sent;
    }
}
