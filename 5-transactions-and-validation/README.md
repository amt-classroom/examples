# 5 - Transactions et validation

Des exemples exécutables sur les transactions, l'isolation, les verrous, les transactions
distribuées, la validation et les écritures en masse. Chaque exemple est un test JUnit qui
tourne sur une vraie base PostgreSQL 18, celle d'un festival de trois jours qui vend des
billets par jour.

Deux sortes de code cohabitent :

- **SQL et JDBC, sans framework** : les tests ouvrent eux-mêmes leurs connexions.
- **Une application Helidon MP 4.5** : Narayana gère les transactions, Hibernate
  l'`EntityManager`, Hibernate Validator les contraintes. Un test annoté `@HelidonTest`
  démarre l'application.

## Exécuter

Docker doit tourner : Testcontainers démarre PostgreSQL dans un conteneur. Sur un Mac avec
OrbStack, Testcontainers a besoin de `DOCKER_HOST=unix://$HOME/.orbstack/run/docker.sock`.

```bash
./mvnw -pl 5-transactions-and-validation verify
```

Les écritures en masse prennent environ deux minutes. Ce que chaque test écrit (les erreurs de
PostgreSQL, le SQL de Hibernate, les durées) se trouve dans
`target/surefire-reports/<classe>-output.txt`.

## Les exemples

Les paquets sont sous `ch.heigvd.amt`.

| Notion                                   | Code                                                                        | Test                                    |
| ---------------------------------------- | --------------------------------------------------------------------------- | --------------------------------------- |
| Transaction JDBC, autocommit, `CHECK`    | `transactions.Reservations`                                                 | `JdbcTransactionTest`                   |
| `@Transactional` et les exceptions       | `transactions.ReservationService`                                           | `TransactionalTest`                     |
| Anomalies et niveaux d'isolation         |                                                                             | `isolation.AnomaliesTest`               |
| Verrouillage pessimiste                  | `locking.StockService`                                                      | `PessimisticLockTest`                   |
| Verrouillage optimiste                   | `locking.Concert`                                                           | `OptimisticLockTest`                    |
| Mise à jour atomique                     | `locking.StockService`                                                      | `AtomicUpdateTest`                      |
| Contrainte d'unicité                     |                                                                             | `UniqueConstraintTest`                  |
| Délais d'attente                         |                                                                             | `TimeoutTest`                           |
| Interblocage                             |                                                                             | `DeadlockTest`                          |
| Validation en deux phases                |                                                                             | `distributed.PreparedTransactionTest`   |
| Transaction distribuée en XA             | `distributed.SaleService`                                                   | `DistributedTest`                       |
| Contraintes et validation des paramètres | `validation.ReservationRequest`, `ValidationService`, `ReservationResource` | `ValidationTest`                        |
| Validation des entités                   | `validation.Reservation`                                                    | `EntityValidationTest`                  |
| Écritures en masse                       | `bulk.Imports`                                                              | `JdbcImportTest`, `HibernateImportTest` |

Une ligne sans code ne montre que du SQL : le test l'envoie directement à PostgreSQL.

## Ce qui sert à faire tourner les exemples

- **Une seule base.** Les tests JDBC et l'application partagent la base `ticketing` et sa
  table `ticket_stock`. Helidon la recrée d'après l'entité `TicketStock` à chaque démarrage ;
  un test JDBC la crée s'il ne la trouve pas.
- **Deux fichiers d'unités de persistance.** `persistence.xml` ne contient que l'unité JTA de
  l'application, car Helidon prend chaque unité de ce fichier pour la sienne. Les tests qui
  utilisent Hibernate seul lisent leurs unités dans
  `src/test/resources/META-INF/persistence-resource-local.xml`.
- **Les deux bases en XA.** Le pool HikariCP de Helidon ne fait pas de XA :
  `EnlistingDataSource` inscrit chaque connexion dans la transaction de Narayana. Les bases
  `sales` et `accounting` journalisent chaque requête, pour que `DistributedTest` voie les
  `PREPARE TRANSACTION`.
