package ch.heigvd.amt.transactions;

/** Il ne reste pas assez de billets ce jour-là. Une exception vérifiée, comme une règle métier. */
public class SoldOutException extends Exception {

    private static final long serialVersionUID = 1L;

    public SoldOutException(String day) {
        super(day + " is sold out");
    }
}
