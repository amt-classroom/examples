package ch.heigvd.amt.proxies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class LoggingHandler implements InvocationHandler {

    private final Object target;

    public LoggingHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("-> " + method.getName() + Arrays.toString(args)); // avant l'appel
        try {
            return method.invoke(target, args); // transmet l'appel à la cible, par reflection
        } catch (InvocationTargetException e) {
            throw e.getCause(); // relance l'exception d'origine, sans l'enveloppe
        }
    }
}
