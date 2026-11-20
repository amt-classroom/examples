package ch.heigvd.amt.library;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/** L'éditeur produit les livres. Un qualifier distingue ces deux producers du même type. */
@ApplicationScoped
public class Publisher {
    @Produces @HeroicFantasy
    Book theLordOfTheRings() {
        return new Book("Le Seigneur des anneaux", "J. R. R. Tolkien");
    }

    @Produces @ScienceFiction
    Book dune() {
        return new Book("Dune", "Frank Herbert");
    }
}
