package ch.heigvd.amt.library;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

/**
 * Le bibliothécaire prête et reprend les livres de l'étagère. @Bell place la cloche autour de
 * ses deux méthodes. Il signale chaque retour par un event, sans connaître ceux qui l'observent.
 */
@Bell
@ApplicationScoped
public class Librarian {
    @Inject
    Shelf shelf;

    @Inject @Returned
    Event<Book> returned;

    public Book lend(String title, String borrower) {
        return shelf.take(title, borrower);
    }

    public void takeBack(Book book) {
        book.giveBack();
        shelf.put(book);
        returned.fire(book);
    }
}
