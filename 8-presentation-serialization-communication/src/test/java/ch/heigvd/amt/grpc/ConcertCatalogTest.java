package ch.heigvd.amt.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.heigvd.amt.protobuf.Concert;
import ch.heigvd.amt.protobuf.ConcertId;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.helidon.common.tls.Tls;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import io.helidon.webclient.grpc.GrpcClient;
import io.helidon.webclient.grpc.GrpcClientMethodDescriptor;
import io.helidon.webclient.grpc.GrpcServiceClient;
import io.helidon.webclient.grpc.GrpcServiceDescriptor;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;

/** Un client gRPC appelle Find comme une méthode : la requête et la réponse sont des messages Protobuf. */
@HelidonTest
class ConcertCatalogTest {

    @Inject
    WebTarget target; // pour connaître le port du serveur

    @Test
    void findReturnsTheConcert() {
        Concert concert = catalog().unary("Find", ConcertId.newBuilder().setId(1).build());

        assertEquals("Zaho de Sagazan", concert.getArtist());
        assertEquals("2027-07-09T21:00", concert.getStartsAt());
    }

    /** gRPC a ses propres codes de statut : l'exception de la méthode arrive en INTERNAL, pas en 500. */
    @Test
    void anUnknownConcertFailsWithAGrpcStatus() {
        var e = assertThrows(StatusRuntimeException.class,
                () -> catalog().unary("Find", ConcertId.newBuilder().setId(42).build()));

        assertEquals(Status.Code.INTERNAL, e.getStatus().getCode());
    }

    private GrpcServiceClient catalog() {
        var client = GrpcClient.builder()
                .baseUri("http://localhost:" + target.getUri().getPort())
                .tls(Tls.builder().enabled(false).build())
                .build();
        return client.serviceClient(GrpcServiceDescriptor.builder()
                .serviceName("ConcertCatalog")
                .putMethod("Find", GrpcClientMethodDescriptor.unary("ConcertCatalog", "Find")
                        .requestType(ConcertId.class)
                        .responseType(Concert.class)
                        .build())
                .build());
    }
}
