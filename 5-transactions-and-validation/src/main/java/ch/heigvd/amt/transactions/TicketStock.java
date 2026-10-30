package ch.heigvd.amt.transactions;

import org.hibernate.annotations.Check;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Les billets qu'il reste pour un jour du festival. La base refuse un stock négatif. */
@Entity
@Table(name = "ticket_stock")
@Check(constraints = "remaining >= 0")
public class TicketStock {

    @Id
    private String day;

    private int remaining;

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }
}
