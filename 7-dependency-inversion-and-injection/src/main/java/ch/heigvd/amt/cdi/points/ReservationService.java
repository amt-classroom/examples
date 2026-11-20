package ch.heigvd.amt.cdi.points;

import ch.heigvd.amt.festival.Mailer;
import ch.heigvd.amt.festival.PaymentGateway;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Les trois points d'injection : le champ, le constructeur, la méthode. */
@ApplicationScoped
public class ReservationService {
    @Inject Mailer mailer;                  // champ
    private final PaymentGateway gateway;
    private Statistics statistics;

    @Inject                                 // constructeur
    public ReservationService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    @Inject                                 // méthode
    void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    /** Vrai quand le conteneur a rempli les trois points d'injection. */
    public boolean isComplete() {
        return mailer != null && gateway != null && statistics != null;
    }
}
