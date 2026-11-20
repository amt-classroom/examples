package ch.heigvd.amt.library;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;
import jakarta.interceptor.Interceptor;

/**
 * Le decorator de l'étagère : il tamponne chaque livre qui en sort et le marque prêté. Il
 * connaît l'interface Shelf, donc le sens de take : c'est de la logique métier.
 */
@Decorator
@Priority(Interceptor.Priority.APPLICATION)
public abstract class StampingShelf implements Shelf {
    @Inject @Delegate
    Shelf shelf;

    public Book take(String title, String borrower) {
        Book book = shelf.take(title, borrower);
        book.lendTo(borrower);
        System.out.println("Stamped " + title + " for " + borrower);
        return book;
    }
}
