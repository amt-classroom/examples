package ch.heigvd.amt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Une étiquette, sans effet propre : seul un lecteur, par reflection, lui donne un sens. */
@Retention(RetentionPolicy.RUNTIME) // visible par reflection à l'exécution
@Target(ElementType.FIELD) // ne peut annoter qu'un champ
public @interface Hidden {
}
