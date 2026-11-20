package ch.heigvd.amt.library;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/** Le responsable observe chaque retour. Le bibliothécaire ne dépend pas de lui. */
@ApplicationScoped
public class Manager {
    void onReturn(@Observes @Returned Book book) {
        System.out.println("Manager: " + book.title() + " is back");
    }
}
