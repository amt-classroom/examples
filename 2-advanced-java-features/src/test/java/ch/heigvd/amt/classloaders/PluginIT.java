package ch.heigvd.amt.classloaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Test;

/** Charge le JAR du plugin, absent du classpath : un test d'intégration, car le JAR n'existe qu'après package. */
class PluginIT {

    @Test
    void aPluginJarIsInvisibleUntilLoaded() {
        // Délégation : l'application ne voit pas la classe du plugin...
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("ch.heigvd.amt.classloaders.plugin.CsvExporter"));
    }

    @Test
    void serviceLoaderFindsThePluginOnceItsJarIsLoaded() throws Exception {
        // ...mais ServiceLoader le trouve une fois le JAR chargé.
        var jar = Path.of(System.getProperty("plugin.jar")).toUri().toURL();
        var loader = new URLClassLoader(new URL[] { jar }, getClass().getClassLoader());

        var names = ServiceLoader.load(Exporter.class, loader).stream()
            .map(ServiceLoader.Provider::get)
            .map(Exporter::name)
            .toList();

        assertEquals(List.of("csv"), names);
    }
}
