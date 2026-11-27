package ch.heigvd.amt.rmi;

import static ch.heigvd.amt.Output.printed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * RMI entre deux JVM : le serveur tourne dans un processus à part, le client dans celle du test.
 */
class RmiTest {

    @Test
    void theClientCallsAnObjectInAnotherJvm() throws Exception {
        int port = freePort();
        Process server = startServer(port);
        try {
            assertEquals(
                    List.of("Ticket{holder='Ada', quantity=2, accessCode=null}"),
                    printed(() -> {
                        try {
                            Client.main(new String[] {String.valueOf(port)});
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }));
        } finally {
            stop(server);
        }
    }

    @Test
    void theStubIsADynamicProxy() throws Exception {
        int port = freePort();
        Process server = startServer(port);
        try {
            var office = LocateRegistry.getRegistry("localhost", port).lookup("office");
            System.out.println(office);

            assertTrue(Proxy.isProxyClass(office.getClass()));
            assertTrue(office instanceof TicketOffice);
        } finally {
            stop(server);
        }
    }

    /**
     * Le serveur disparaît entre deux appels : le second lève une RemoteException, celle que
     * l'interface oblige à déclarer. Sa sous-classe dépend de l'instant : ConnectException si le
     * client doit rouvrir une connexion, UnmarshalException s'il en réutilise une qui vient de
     * se fermer.
     */
    @Test
    void theCallFailsOnItsOwnOnceTheServerIsGone() throws Exception {
        int port = freePort();
        Process server = startServer(port);
        TicketOffice office;
        try {
            office = (TicketOffice) LocateRegistry.getRegistry("localhost", port).lookup("office");
            office.buy("Ada", 2);
        } finally {
            stop(server);
        }

        var e = assertThrows(RemoteException.class, () -> office.buy("Ada", 2));
        System.out.println(e.getClass().getName());
    }

    /**
     * Le piège qui explique le champ statique de Server : RMI ne retient un objet exporté que par
     * une référence faible. Tant qu'aucun client ne tient son stub, le ramasse-miettes le reprend.
     */
    @Test
    void anExportedObjectNobodyHoldsIsCollected() throws Exception {
        int port = freePort();
        var registry = LocateRegistry.createRegistry(port);
        try {
            var held = new WeakReference<>(exportAndBind(registry));
            for (int i = 0; i < 50 && held.get() != null; i++) {
                System.gc();
                Thread.sleep(100);
            }
            assertNull(held.get());

            var office = (TicketOffice) LocateRegistry.getRegistry("localhost", port).lookup("office");
            var e = assertThrows(NoSuchObjectException.class, () -> office.buy("Ada", 2));
            System.out.println(e.getMessage());

            assertEquals("no such object in table", e.getMessage());
        } finally {
            UnicastRemoteObject.unexportObject(registry, true);
        }
    }

    /** Exporte une billetterie sans garder de référence vers elle, comme un serveur naïf. */
    private static TicketOffice exportAndBind(Registry registry) throws Exception {
        var office = new FestivalTicketOffice();
        registry.rebind("office", UnicastRemoteObject.exportObject(office, 0));
        return office;
    }

    /**
     * Lance Server dans une autre JVM et attend sa première ligne. Le stub porte l'adresse que le
     * serveur se connaît : on la fixe à localhost pour ne pas dépendre du réseau de la machine.
     */
    private static Process startServer(int port) throws IOException {
        var java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        Process server = new ProcessBuilder(
                java, "-Djava.rmi.server.hostname=localhost",
                "-cp", Path.of("target", "classes").toString(),
                Server.class.getName(), String.valueOf(port))
                .redirectErrorStream(true)
                .start();
        var out = new BufferedReader(new InputStreamReader(server.getInputStream()));
        String line = out.readLine();
        if (!("Ticket office ready on port " + port).equals(line)) {
            server.destroyForcibly();
            throw new IllegalStateException("Server did not start: " + line);
        }
        return server;
    }

    private static void stop(Process server) throws InterruptedException {
        server.destroyForcibly().waitFor();
    }

    private static int freePort() throws IOException {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
