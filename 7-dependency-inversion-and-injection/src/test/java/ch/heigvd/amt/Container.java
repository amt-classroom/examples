package ch.heigvd.amt;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * Weld, le conteneur CDI de Helidon, démarré à la main avec quelques classes seulement. Les
 * tests s'en servent pour observer ce que {@code @HelidonTest} ne laisse pas voir : un
 * démarrage que le conteneur refuse, et l'arrêt du conteneur.
 */
public final class Container {

    private Container() {
    }

    /** Démarre un conteneur qui ne connaît que ces classes. Le fermer l'arrête. */
    public static WeldContainer start(Class<?>... beans) {
        return new Weld().disableDiscovery().addBeanClasses(beans).initialize();
    }
}
