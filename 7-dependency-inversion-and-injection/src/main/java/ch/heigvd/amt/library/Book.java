package ch.heigvd.amt.library;

import java.util.Optional;

/** Un livre : un titre, un auteur et, tant qu'il est prêté, un emprunteur. */
public class Book {
    private final String title;
    private final String author;
    private String borrower;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String title() {
        return title;
    }

    public String author() {
        return author;
    }

    public Optional<String> borrower() {
        return Optional.ofNullable(borrower);
    }

    public void lendTo(String borrower) {
        this.borrower = borrower;
    }

    public void giveBack() {
        this.borrower = null;
    }
}
