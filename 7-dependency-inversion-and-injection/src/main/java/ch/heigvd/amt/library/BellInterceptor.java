package ch.heigvd.amt.library;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/** L'interceptor fait sonner la cloche avant chaque appel d'un bean qui porte @Bell. */
@Bell
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class BellInterceptor {
    @AroundInvoke
    Object ring(InvocationContext ctx) throws Exception {
        System.out.println("Ding!");
        return ctx.proceed();
    }
}
