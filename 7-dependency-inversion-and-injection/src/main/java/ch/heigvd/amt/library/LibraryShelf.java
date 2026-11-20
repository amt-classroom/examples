package ch.heigvd.amt.library;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * L'étagère de la bibliothèque. À sa création, le conteneur lui injecte les livres de fantasy,
 * puis appelle fill : dans le constructeur, le champ books vaudrait encore null.
 */
@ApplicationScoped
public class LibraryShelf implements Shelf {
    @Inject @HeroicFantasy
    Instance<Book> books;

    private final Map<String, Book> byTitle = new HashMap<>();

    @PostConstruct
    void fill() {
        books.forEach(this::put);
    }

    public Book take(String title, String borrower) {
        Book book = byTitle.remove(title);
        if (book == null) {
            throw new IllegalArgumentException("No book titled " + title);
        }
        return book;
    }

    public void put(Book book) {
        byTitle.put(book.title(), book);
    }
}
