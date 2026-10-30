package ch.heigvd.amt.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.heigvd.amt.Postgres;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/** Jakarta Validation dans Helidon : à la main, par le conteneur, puis par HTTP. */
@ExtendWith(Postgres.class)
@HelidonTest
class ValidationTest {

    static final ReservationRequest VALID =
        new ReservationRequest("Jane Doe", "jane@example.com", List.of("FRIDAY", "SATURDAY"), 2);
    static final ReservationRequest INVALID =
        new ReservationRequest("Jane Doe", "jane.example.com", List.of("FRIDAY"), 6);

    @Inject
    ValidationService service;

    @Inject
    WebTarget target;

    @Test
    void manualValidationPrintsEachViolation() {
        String printed = printed(() -> service.manualValidation(INVALID));

        assertTrue(printed.contains("Email is not valid"), printed);
        assertTrue(printed.contains("Reserve at most four tickets"), printed);
    }

    @Test
    void theContainerValidatesTheParameter() {
        var e = assertThrows(ConstraintViolationException.class, () -> service.automaticValidation(INVALID));

        assertEquals(2, e.getConstraintViolations().size());
        e.getConstraintViolations().forEach(v -> System.out.println(v.getPropertyPath() + ": " + v.getMessage()));
    }

    @Test
    void anInvalidRequestGets400() {
        assertEquals(400, post(INVALID));
    }

    @Test
    void aValidRequestGets201ThenTheLastTicketsGet409() throws SQLException {
        Postgres.resetTicketStock(2, 2, 2);

        assertEquals(201, post(VALID));
        assertEquals(409, post(VALID)); // vendredi est complet
    }

    private int post(ReservationRequest request) {
        try (Response response = target.path("/reservations").request().post(Entity.json(request))) {
            return response.getStatus();
        }
    }

    /** Ce que l'action écrit sur System.out. */
    private static String printed(Runnable action) {
        PrintStream original = System.out;
        var buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }
}
