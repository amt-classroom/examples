package ch.heigvd.amt.proxies;

import java.lang.reflect.Proxy;

public final class Proxies {

    private Proxies() {
    }

    public static <T> T logged(Class<T> type, T target) {
        return type.cast(Proxy.newProxyInstance(
            type.getClassLoader(), // pour définir la classe du proxy
            new Class<?>[] { type }, // les interfaces qu'il implémente
            new LoggingHandler(target))); // qui reçoit tous les appels
    }
}
