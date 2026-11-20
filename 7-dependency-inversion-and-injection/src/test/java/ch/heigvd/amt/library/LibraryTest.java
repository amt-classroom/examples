package ch.heigvd.amt.library;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/** Une bibliothèque : producers, qualifiers, callback, decorator, interceptor et event réunis. */
@HelidonTest
@DisableDiscovery
@AddBean(Publisher.class)
@AddBean(LibraryShelf.class)
@AddBean(Librarian.class)
@AddBean(Manager.class)
@AddBean(value = StampingShelf.class, scope = Dependent.class)
@AddBean(value = BellInterceptor.class, scope = Dependent.class)
class LibraryTest {

    @Inject
    Librarian librarian;

    @Test
    void theDecoratorMarksALentBookAsBorrowed() {
        Book book = librarian.lend("Le Seigneur des anneaux", "Ada");
        assertEquals(Optional.of("Ada"), book.borrower());

        librarian.takeBack(book);
        assertEquals(Optional.empty(), book.borrower());
    }

    @Test
    void theBellRingsAndTheManagerHearsOfTheReturn() {
        assertEquals(List.of(
                "Ding!",
                "Stamped Le Seigneur des anneaux for Ada",
                "Ding!",
                "Manager: Le Seigneur des anneaux is back"),
                printed(() -> librarian.takeBack(librarian.lend("Le Seigneur des anneaux", "Ada"))));
    }

    @Test
    void theShelfHoldsOnlyHeroicFantasy() {
        assertThrows(IllegalArgumentException.class, () -> librarian.lend("Dune", "Ada"));
    }
}
