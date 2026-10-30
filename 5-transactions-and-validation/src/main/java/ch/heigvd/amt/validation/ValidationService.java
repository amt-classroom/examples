package ch.heigvd.amt.validation;

import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

/** Valider une demande à la main, ou laisser le conteneur la valider avant l'appel. */
@ApplicationScoped
public class ValidationService {
    @Inject Validator validator;

    public void manualValidation(ReservationRequest request) {
        Set<ConstraintViolation<ReservationRequest>> violations =
                validator.validate(request);
        violations.forEach(v -> System.out.println(v.getMessage()));
    }

    public void automaticValidation(
            @Valid ReservationRequest request) {
        // validée par le conteneur avant l'appel
    }
}
