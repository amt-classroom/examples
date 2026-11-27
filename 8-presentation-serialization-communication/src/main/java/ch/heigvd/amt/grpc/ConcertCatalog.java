package ch.heigvd.amt.grpc;

import ch.heigvd.amt.presentation.ConcertService;
import ch.heigvd.amt.protobuf.Concert;
import ch.heigvd.amt.protobuf.ConcertId;
import io.helidon.grpc.api.Grpc;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Le programme par gRPC : la même couche métier, derrière une autre présentation. */
@Grpc.GrpcService("ConcertCatalog")
@ApplicationScoped
public class ConcertCatalog {

    @Inject ConcertService concerts;

    @Grpc.Unary("Find")
    public Concert find(ConcertId id) {
        var concert = concerts.find(id.getId()).orElseThrow();
        return Concert.newBuilder()
                .setId(concert.getId())
                .setArtist(concert.getArtist())
                .setStage(concert.getStage())
                .setStartsAt(concert.getStartsAt().toString())
                .build();
    }
}
