package ch.heigvd.amt.conditional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

/** Une scène par HTTP : l'ETag porte sa version, If-Match la renvoie avec la modification. */
@Path("/stages/{id}")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class StageResource {
    @Inject StageService stages;

    @GET
    public Response find(@PathParam("id") long id) {
        return respond(stages.find(id));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") long id, StageDto dto,
                           @Context Request request) {
        var stale = request.evaluatePreconditions(tag(stages.find(id)));
        if (stale != null) {
            return stale.build(); // 412 Precondition Failed
        }
        return respond(stages.update(id, dto.name(), dto.capacity()));
    }

    private static Response respond(Stage stage) {
        return Response.ok(new StageDto(stage.name(), stage.capacity()))
                .tag(tag(stage))
                .build();
    }

    private static EntityTag tag(Stage stage) {
        return new EntityTag(String.valueOf(stage.version()));
    }
}
