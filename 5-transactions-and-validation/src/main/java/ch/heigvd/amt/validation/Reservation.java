package ch.heigvd.amt.validation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Une réservation enregistrée. Hibernate vérifie ses contraintes avant de l'écrire. */
@Entity
public class Reservation {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull @Email
    private String email;

    @Min(1) @Max(4)
    private int quantity;

    protected Reservation() {} // pour JPA

    public Reservation(String email, int quantity) {
        this.email = email;
        this.quantity = quantity;
    }
}
