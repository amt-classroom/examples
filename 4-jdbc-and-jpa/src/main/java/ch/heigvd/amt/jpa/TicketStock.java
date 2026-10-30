package ch.heigvd.amt.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** Les billets qu'il reste à vendre : une seule ligne, que réservations et libérations se disputent. */
@Entity
public class TicketStock {

    @Id
    private Long id;

    private int remaining;

    protected TicketStock() {} // pour le fournisseur

    public TicketStock(long id, int remaining) {
        this.id = id;
        this.remaining = remaining;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }
}
