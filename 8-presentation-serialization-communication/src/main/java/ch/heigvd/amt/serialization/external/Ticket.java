package ch.heigvd.amt.serialization.external;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** Un billet qui écrit et relit lui-même ses champs. */
public class Ticket implements Externalizable {
    private static final long serialVersionUID = 1L;

    private String holder;
    private int quantity;

    public Ticket() {} // requis : Java crée l'objet, puis appelle readExternal

    public Ticket(String holder, int quantity) {
        this.holder = holder;
        this.quantity = quantity;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(holder);
        out.writeInt(quantity);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        holder = in.readUTF();
        quantity = in.readInt();
    }

    @Override
    public String toString() {
        return "Ticket{holder='" + holder + "', quantity=" + quantity + "}";
    }
}
