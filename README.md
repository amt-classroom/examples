# Illustrations AMT

Des exemples exécutables pour le cours AMT, un module Maven par sujet. Chaque exemple est un
test JUnit : on peut le lire, le lancer et le modifier pour voir ce qui change. Java 21 ou
plus récent.

Ce dépôt ne contient pas les corrections des laboratoires.

| Module                                                                          | Sujet                                      | Contenu                                                                                                                                                |
| ------------------------------------------------------------------------------- | ------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [`2-advanced-java-features`](2-advanced-java-features/)                         | Fonctionnalités avancées de Java           | classloaders, reflection, annotations à l'exécution, proxies                                                                                           |
| [`2-advanced-java-features-processor`](2-advanced-java-features-processor/)     | Fonctionnalités avancées de Java           | `@Describe` et l'annotation processor, compilés avant le reste                                                                                         |
| [`4-jdbc-and-jpa`](4-jdbc-and-jpa/)                                             | JDBC et JPA                                | pilotes, connexions, pool ; entités, contexte de persistance, requêtes, chargement                                                                     |
| [`5-transactions-and-validation`](5-transactions-and-validation/)               | Transactions et validation                 | transactions en JDBC, isolation, verrous, délais, écritures en masse, deux phases ; une application Helidon : `@Transactional`, XA, Jakarta Validation |
| [`6-database-evolution-and-migration`](6-database-evolution-and-migration/)     | Évolution et migration de bases de données | génération du schéma, Flyway, migrations de données, rollback, expand and contract, jOOQ ; une application Helidon qui migre sa base au démarrage      |
| [`7-dependency-inversion-and-injection`](7-dependency-inversion-and-injection/) | Inversion et injection de dépendances      | inversion des dépendances, injection, inversion de contrôle ; Jakarta CDI dans Helidon ; doublures de test avec Mockito                                |
| [`8-presentation-serialization-communication`](8-presentation-serialization-communication/) | Présentation, sérialisation et communication | une application Helidon : DTO, mapper, ressource Jakarta REST, erreurs, idempotence, `If-Match`, contrat OpenAPI ; sérialisation Java et RMI ; XML (parseurs, JAXB, XSD, XJC, une déclaration d'impôt eCH-0119) ; JSON ; Protobuf et gRPC |
| [`tika-and-poi`](tika-and-poi/) | Apache Tika et Apache POI | un rapport PDF et ses tableaux, lus avec Tika et réécrits en classeur Excel avec POI |

## Compiler et exécuter

```bash
./mvnw verify
```

`./mvnw` télécharge la version de Maven fixée au premier lancement. Les tests de
`4-jdbc-and-jpa`, de `5-transactions-and-validation` et de
`6-database-evolution-and-migration` démarrent PostgreSQL dans un conteneur : Docker doit
tourner. `6-database-evolution-and-migration` en a aussi besoin pour compiler, car jOOQ génère
ses classes depuis une vraie base. `8-presentation-serialization-communication` télécharge
`protoc`, le compilateur de Protobuf, à sa première compilation.
