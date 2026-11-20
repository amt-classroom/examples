package ch.heigvd.amt.cdi.scopes;

import jakarta.enterprise.context.RequestScoped;

/** Une instance par requête : chaque requête a son acheteur. */
@RequestScoped
public class CurrentBuyer {
    private String email;

    public String email() {
        return email;
    }

    public void identify(String email) {
        this.email = email;
    }
}
