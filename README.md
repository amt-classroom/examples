# Illustrations AMT

Des exemples exécutables pour le cours AMT, un module Maven par sujet. Chaque exemple est un
test JUnit : on peut le lire, le lancer et le modifier pour voir ce qui change. Java 21 ou
plus récent.

Ce dépôt ne contient pas les corrections des laboratoires.

| Module                                                                      | Sujet                            | Contenu                                                                                                                                                |
| --------------------------------------------------------------------------- | -------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| [`2-advanced-java-features`](2-advanced-java-features/)                     | Fonctionnalités avancées de Java | classloaders, reflection, annotations à l'exécution, proxies                                                                                           |
| [`2-advanced-java-features-processor`](2-advanced-java-features-processor/) | Fonctionnalités avancées de Java | `@Describe` et l'annotation processor, compilés avant le reste                                                                                         |
| [`4-jdbc-and-jpa`](4-jdbc-and-jpa/)                                         | JDBC et JPA                      | pilotes, connexions, pool ; entités, contexte de persistance, requêtes, chargement                                                                     |
| [`5-transactions-and-validation`](5-transactions-and-validation/)           | Transactions et validation       | transactions en JDBC, isolation, verrous, délais, écritures en masse, deux phases ; une application Helidon : `@Transactional`, XA, Jakarta Validation |

## Compiler et exécuter

```bash
./mvnw verify
```

`./mvnw` télécharge la version de Maven fixée au premier lancement. Les tests de
`4-jdbc-and-jpa` et de `5-transactions-and-validation` démarrent PostgreSQL dans un
conteneur : Docker doit tourner.
