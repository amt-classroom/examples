package ch.heigvd.amt.cdi.scopes;

import jakarta.enterprise.context.Dependent;

/** Une instance par point d'injection : personne ne la partage. */
@Dependent
public class Receipt {
}
