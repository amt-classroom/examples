package ch.heigvd.amt.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Exporte la billetterie et publie son stub dans un registre. La JVM reste en vie après la fin
 * de main, tant que l'objet est exporté.
 */
public class Server {

    /**
     * Le serveur garde lui-même une référence vers la billetterie. RMI ne la retient que par une
     * référence faible : sans ce champ, le ramasse-miettes la reprend tant qu'aucun client ne
     * tient son stub, et l'appel suivant échoue.
     */
    private static final TicketOffice OFFICE = new FestivalTicketOffice();

    public static void main(String[] args) throws RemoteException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : Registry.REGISTRY_PORT;

        var stub = UnicastRemoteObject.exportObject(OFFICE, 0);
        var registry = LocateRegistry.createRegistry(port);
        registry.rebind("office", stub);

        System.out.println("Ticket office ready on port " + port);
    }
}
