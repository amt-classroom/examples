package ch.heigvd.amt.serialization;

import java.io.Serializable;

/** Un billet que Java sait écrire en bytes : il suffit de le déclarer Serializable. */
public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String holder;
    private final int quantity;
    private final transient String accessCode;

    public Ticket(String holder, int quantity, String accessCode) {
        this.holder = holder;
        this.quantity = quantity;
        this.accessCode = accessCode;
    }

    @Override
    public String toString() {
        return "Ticket{holder='" + holder + "', quantity=" + quantity
                + ", accessCode=" + accessCode + "}";
    }
}
