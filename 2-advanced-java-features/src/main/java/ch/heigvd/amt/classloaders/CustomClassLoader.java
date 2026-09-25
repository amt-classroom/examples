package ch.heigvd.amt.classloaders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Charge des classes depuis un répertoire hors classpath ; chaque instance isole ses classes des autres. */
public class CustomClassLoader extends ClassLoader {

    private final Path root;

    public CustomClassLoader(ClassLoader parent, Path root) {
        super(parent); // conserve la délégation : le parent est toujours essayé en premier
        this.root = root;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        var file = root.resolve(name.replace('.', '/') + ".class");
        try {
            var bytes = Files.readAllBytes(file);
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
