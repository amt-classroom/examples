package ch.heigvd.amt.rmi;

import ch.heigvd.amt.serialization.Ticket;

/** La billetterie du serveur : un objet ordinaire, qui ignore qu'on l'appelle à distance. */
public class FestivalTicketOffice implements TicketOffice {

    @Override
    public Ticket buy(String holder, int quantity) {
        return new Ticket(holder, quantity, "X7K2");
    }
}
