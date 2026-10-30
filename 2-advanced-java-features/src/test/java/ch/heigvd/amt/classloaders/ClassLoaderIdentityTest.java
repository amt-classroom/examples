package ch.heigvd.amt.classloaders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Driver;

import org.junit.jupiter.api.Test;

/** Chaque classe connaît le classloader qui l'a chargée : Bootstrap, Platform, puis System. */
class ClassLoaderIdentityTest {

    @Test
    void bootstrapLoadsTheJdkCore() {
        assertNull(String.class.getClassLoader()); // chargée nativement, par Bootstrap
    }

    @Test
    void platformLoadsAJdkModule() {
        assertNotNull(Driver.class.getClassLoader());
        assertNotSame(String.class.getClassLoader(), Driver.class.getClassLoader());
    }

    @Test
    void systemLoadsTheApplication() {
        assertNotNull(Sample.class.getClassLoader());
        assertNotSame(Driver.class.getClassLoader(), Sample.class.getClassLoader());
    }

    @Test
    void aClassLoaderFindsAPackageAndLoadsAClassByName() throws ClassNotFoundException {
        var loader = Sample.class.getClassLoader();
        assertNotNull(loader.getResource("ch/heigvd/amt/classloaders"));
        assertEquals(Sample.class, loader.loadClass("ch.heigvd.amt.classloaders.Sample"));
    }
}
