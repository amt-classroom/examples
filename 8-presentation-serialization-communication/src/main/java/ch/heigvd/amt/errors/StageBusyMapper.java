package ch.heigvd.amt.errors;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/** Traduit l'exception du métier en réponse HTTP : la couche métier ignore le code 409. */
@Provider
public class StageBusyMapper
        implements ExceptionMapper<StageBusyException> {
    @Override
    public Response toResponse(StageBusyException e) {
        var problem = new Problem("Stage busy", 409, e.getMessage());
        return Response.status(409)
                .type("application/problem+json")
                .entity(problem)
                .build();
    }
}
