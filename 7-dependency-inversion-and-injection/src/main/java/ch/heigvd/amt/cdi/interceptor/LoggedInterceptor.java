package ch.heigvd.amt.cdi.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/** Le conteneur appelle log à la place de la méthode du bean ; proceed poursuit l'appel. */
@Logged
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggedInterceptor {
    @AroundInvoke
    Object log(InvocationContext ctx) throws Exception {
        String name = ctx.getMethod().getName();
        System.out.println("Before " + name);
        try {
            return ctx.proceed();
        } finally {
            System.out.println("After " + name);
        }
    }
}
