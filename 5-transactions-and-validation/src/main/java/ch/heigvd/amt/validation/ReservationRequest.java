package ch.heigvd.amt.validation;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/** La demande de réservation qu'envoie le formulaire. Chaque contrainte porte son message. */
public record ReservationRequest(
        @NotBlank(message = "Name cannot be blank")
        String name,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email is not valid")
        String email,

        @NotEmpty(message = "Choose at least one day")
        List<String> days,

        @Min(value = 1, message = "Reserve at least one ticket")
        @Max(value = 4, message = "Reserve at most four tickets")
        int quantity) {
}
