package ch.heigvd.amt.describe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marque un champ que le describer généré masque au lieu de l'écrire. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Hidden {
}
