package ch.heigvd.amt.festival;

/** Un moyen de paiement : il dit s'il sait traiter une méthode, puis débite l'acheteur. */
public interface PaymentGateway {
    boolean supports(String method);

    boolean charge(String email, int amount);
}
