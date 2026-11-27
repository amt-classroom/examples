package ch.heigvd.amt.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Cherche la billetterie dans le registre et lui achète un billet. Le billet reçu est une copie,
 * passée par la sérialisation Java : son champ transient n'a pas fait le voyage.
 */
public class Client {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : Registry.REGISTRY_PORT;

        var registry = LocateRegistry.getRegistry("localhost", port);
        var office = (TicketOffice) registry.lookup("office");
        System.out.println(office.buy("Ada", 2));
    }
}
