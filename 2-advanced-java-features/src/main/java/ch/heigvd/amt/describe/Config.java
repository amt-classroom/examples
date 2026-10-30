package ch.heigvd.amt.describe;

/** Une classe @Describe avec un champ ordinaire et un champ @Hidden. */
@Describe
public class Config {

    String host = "localhost";
    @Hidden String token = "s3cr3t"; // sera masqué dans le code généré
}
