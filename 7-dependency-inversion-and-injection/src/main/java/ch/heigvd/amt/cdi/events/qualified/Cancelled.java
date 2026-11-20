package ch.heigvd.amt.cdi.events.qualified;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

/** Un qualifier qui trie les events d'un même type. */
@Qualifier
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface Cancelled {}
