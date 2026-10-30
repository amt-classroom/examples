package ch.heigvd.amt.reflection;

import java.util.ArrayList;

/** Lit les champs de n'importe quel objet, sans connaître sa classe. */
public final class Describer {

    private Describer() {
    }

    public static String describe(Object object) throws IllegalAccessException {
        var parts = new ArrayList<String>();
        for (var field : object.getClass().getDeclaredFields()) { // tous les champs, même privés
            field.setAccessible(true); // autorise la lecture malgré private
            parts.add(field.getName() + "=" + field.get(object)); // lit la valeur par reflection
        }
        return object.getClass().getSimpleName() + parts;
    }
}
