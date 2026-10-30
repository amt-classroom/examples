# 4 - JDBC et JPA

JDBC et JPA sur une vraie base PostgreSQL 18, celle d'un festival avec ses scènes et ses
concerts. Chaque exemple est un test JUnit. Testcontainers démarre PostgreSQL dans un
conteneur : Docker doit tourner.

| Notion                                               | Code et tests                                             | Ce qu'il vérifie                                                                                                                         |
| ---------------------------------------------------- | --------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| Une API, des pilotes                                 | `jdbc.DriversTest`                                        | `ServiceLoader` trouve les pilotes ; le même code sur PostgreSQL et SQLite                                                               |
| Connexion, source de données, libérer les ressources | `jdbc.ConnectionTest`                                     | `SELECT version()`, `PGSimpleDataSource`, la connexion fermée, le refus à 100 connexions                                                 |
| Pool de connexions                                   | `jdbc.PoolTest`                                           | `close()` rend la connexion, la onzième attend puis échoue, la famine du pool et la taille qui l'évite                                   |
| Entité et EntityManager                              | `jpa.Concert`, `jpa.ConcertService`, `ConcertServiceTest` | `persist` écrit au commit, `find` lit par la clé, une instance par clé                                                                   |
| Relation laissée aux défauts                         | `defaults`, `OneToManyDefaultsTest`                       | la table de jointure que produit un `@OneToMany` seul                                                                                    |
| Côté propriétaire d'une relation                     | `jpa.Stage`, `OwningSideTest`                             | la clé étrangère `stage_id`, et la liste inverse qui n'écrit rien                                                                        |
| Cycle de vie d'une entité                            | `LifecycleTest`                                           | nouvelle, gérée, détachée ; le piège de `merge`                                                                                          |
| Écrire sans appeler save                             | `ConcertService`, `WritingTest`                           | `UPDATE` et `DELETE` au flush, `persist` ignoré, le flush avant une requête                                                              |
| SQL natif, JPQL, requêtes nommées, Criteria          | `jpa.Stage`, `QueriesTest`                                | le nombre de concerts par scène écrit de quatre manières, une requête nommée ; la valeur concaténée qui réécrit la requête               |
| Chargement des relations, problème N+1               | `FetchingTest`, `eager`, `EagerTest`                      | la liste chargée au premier accès, 1 + N requêtes, `JOIN FETCH`, graphe d'entités, `LazyInitializationException` ; `EAGER` ne règle rien |
| Mise à jour perdue                                   | `jpa.TicketStock`, `LostUpdateTest`                       | la mise à jour perdue, l'`UPDATE` conditionnel, et l'entité qu'il laisse périmée                                                         |

Les paquets sont sous `ch.heigvd.amt`. La partie JDBC n'a que des tests : chacun ouvre ses
connexions lui-même.

## Comment lire les tests

- **Le SQL.** `hibernate.show_sql` écrit chaque requête sur `System.out`. Les tests la capturent
  (`Output`) et vérifient le SQL que Hibernate envoie. Le journal complet de chaque classe de
  test est dans `target/surefire-reports/<classe>-output.txt`.
- **Le conteneur.** `ConcertService` porte `@ApplicationScoped`, `@PersistenceContext` et
  `@Transactional`, mais aucun conteneur ne tourne ici. Les tests en tiennent le rôle : ils
  affectent `em` et ouvrent la transaction, puis la valident.

## Exécuter

```bash
./mvnw -pl 4-jdbc-and-jpa verify
```

## Limites voulues

- Le contexte de persistance que gère un conteneur, qui dure une transaction, n'est pas
  vérifié : les tests ouvrent un EntityManager par test.
- Le nommage des colonnes par défaut, le `@OneToMany` unidirectionnel avec `@JoinColumn` et
  `@NamedEntityGraph` n'ont pas d'exemple.
