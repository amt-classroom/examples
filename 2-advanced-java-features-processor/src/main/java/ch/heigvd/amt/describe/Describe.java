package ch.heigvd.amt.describe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marque une classe dont {@link DescriberProcessor} doit générer le describer, à la compilation. */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Describe {
}
