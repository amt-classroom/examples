package ch.heigvd.amt.cdi.decorator;

import ch.heigvd.amt.festival.Mailer;
import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

/**
 * Un decorator implémente l'interface du bean qu'il décore, et le conteneur lui injecte ce
 * bean. Il connaît l'interface : il peut porter de la logique métier.
 */
@Decorator
@Priority(Interceptor.Priority.APPLICATION)
public class SignatureDecorator implements Mailer {
    @Inject @Delegate
    Mailer mailer;

    public void send(String to, String message) {
        mailer.send(to, message + " -- Bain de foule");
    }
}
