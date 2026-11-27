package ch.heigvd.amt.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ch.heigvd.amt.serialization.Ticket;

/**
 * La billetterie, vue par un client d'une autre JVM. Chaque méthode déclare RemoteException :
 * l'appel traverse le réseau et peut échouer sans que la billetterie y soit pour rien.
 */
public interface TicketOffice extends Remote {
    Ticket buy(String holder, int quantity) throws RemoteException;
}
