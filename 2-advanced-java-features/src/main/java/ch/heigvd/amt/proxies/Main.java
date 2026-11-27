package ch.heigvd.amt.proxies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

interface Histoire {
    void raconter();
}

class HistoireOriginale implements Histoire {
    @Override
    public void raconter() {
        System.out.println("Il était une fois, dans un royaume lointain...");
    }
}

class Narrateur implements InvocationHandler {
    private final Object histoire;

    public Narrateur(Object histoire) {
        this.histoire = histoire;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Écoutez attentivement, je vais vous raconter une histoire.");
        Object result = method.invoke(histoire, args);
        System.out.println("Et ils vécurent heureux pour toujours. Fin.");
        return result;
    }
}

public class Main {
    public static void main(String[] args) {
        Histoire histoireOriginale = new HistoireOriginale();
        Narrateur narrateur = new Narrateur(histoireOriginale);
        Histoire histoireNarrée = (Histoire) Proxy.newProxyInstance(
            Histoire.class.getClassLoader(),
            new Class<?>[]{Histoire.class},
            narrateur
        );
        histoireNarrée.raconter();
    }
}
