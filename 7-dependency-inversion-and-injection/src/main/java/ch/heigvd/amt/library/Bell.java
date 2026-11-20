package ch.heigvd.amt.library;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.interceptor.InterceptorBinding;

/** L'interceptor binding : le lien entre un bean et la cloche. */
@InterceptorBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Bell {}
