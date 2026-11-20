package ch.heigvd.amt.festival;

/** Le paiement a échoué : carte refusée, délai dépassé. */
public class PaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PaymentException(String message) {
        super(message);
    }
}
