package ch.heigvd.amt.annotations;

/** Un champ annoté @Hidden parmi d'autres, ordinaires. */
public class Config {

    String host = "localhost";
    @Hidden String token = "s3cr3t"; // annoté : à cacher
}
