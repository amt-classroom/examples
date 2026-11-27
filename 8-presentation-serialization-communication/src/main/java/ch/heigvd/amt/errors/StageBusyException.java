package ch.heigvd.amt.errors;

import java.time.LocalDateTime;

/** Une règle du métier : une scène n'accueille qu'un concert à la fois. */
public class StageBusyException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public StageBusyException(String stage, LocalDateTime startsAt) {
        super(stage + " is taken at " + startsAt);
    }
}
