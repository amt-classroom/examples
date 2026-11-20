package ch.heigvd.amt.library;

/** L'étagère. Elle est une interface pour qu'un decorator puisse se placer devant elle. */
public interface Shelf {
    /** Retire de l'étagère le livre qui porte ce titre, pour cet emprunteur. */
    Book take(String title, String borrower);

    void put(Book book);
}
