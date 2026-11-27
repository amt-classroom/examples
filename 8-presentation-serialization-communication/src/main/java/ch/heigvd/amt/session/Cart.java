package ch.heigvd.amt.session;

import java.io.Serializable;
import java.util.ArrayList;

import jakarta.enterprise.context.SessionScoped;

/** Un panier gardé dans la session HTTP, donc dans la mémoire d'une instance. */
@SessionScoped
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ArrayList<String> days = new ArrayList<>();

    public void add(String day) {
        days.add(day);
    }
}
