# 6 - Évolution et migration de bases de données

Des exemples exécutables sur un schéma qui doit changer alors que ses données restent. Chaque
exemple est un test JUnit qui tourne sur une vraie base PostgreSQL 18, celle d'un festival avec
ses scènes, ses concerts et ses réservations.

Trois sortes de code cohabitent :

- **SQL et Flyway, sans framework** : huit scripts racontent l'histoire de la base. Chaque test
  les applique jusqu'à la version qui l'intéresse.
- **Une application Helidon MP 4.5** : notre code lance Flyway au démarrage, puis Hibernate
  vérifie ses classes. Un test annoté `@HelidonTest` démarre l'application.
- **jOOQ** : le build génère des classes depuis une base migrée, et `StageReport` écrit sa
  requête avec elles.

## Exécuter

Docker doit tourner, même pour compiler : Testcontainers démarre PostgreSQL pour les tests, et un
plugin Maven le démarre pour la génération de jOOQ. Sur un Mac avec OrbStack, ces deux outils ont
besoin de `DOCKER_HOST=unix://$HOME/.orbstack/run/docker.sock`.

```bash
./mvnw -pl 6-database-evolution-and-migration verify
```

Le module prend environ deux minutes, dont près d'une pour les 2 millions de lignes de
`BatchedMigrationTest`. Ce que chaque test écrit (les erreurs de Flyway, les durées mesurées) se
trouve dans `target/surefire-reports/<classe>-output.txt`.

## Les scripts

Ils se trouvent dans `src/main/resources/db/migration`. Leur somme de contrôle dépend de leur
texte exact : `FlywayTest` et `ImmutabilityTest` la vérifient.

| Script                         | Ce qu'il fait                                                           |
| ------------------------------ | ----------------------------------------------------------------------- |
| `V1__create_stage_table`       | crée les scènes                                                         |
| `V2__create_concert_table`     | crée les concerts, avec le nom de leur artiste                          |
| `V3__add_stage_capacity`       | ajoute une colonne facultative                                          |
| `V4__create_artist_table`      | déplace le nom de chaque artiste vers une nouvelle table                |
| `V5__create_reservation_table` | crée les réservations                                                   |
| `V6__normalize_emails`         | met les adresses en minuscules par lots, hors transaction (`.sql.conf`) |
| `V7__expand_stage_label`       | ajoute `label` à côté de `name`, et un trigger qui les synchronise      |
| `V8__contract_stage_label`     | retire le trigger et `name`                                             |

## Les exemples

Les paquets sont sous `ch.heigvd.amt`.

| Notion                                     | Code                                           | Test                                   |
| ------------------------------------------ | ---------------------------------------------- | -------------------------------------- |
| Les modes de génération du schéma          | `generation.Stage`, `generation.renamed.Stage` | `generation.SchemaGenerationTest`      |
| Le script que Hibernate prépare            | `generation.Stage`                             | `generation.SchemaGenerationTest`      |
| Flyway et son historique                   | `V1` à `V3`                                    | `flyway.FlywayTest`                    |
| L'immuabilité des scripts                  | `V1` à `V3`                                    | `flyway.ImmutabilityTest`              |
| La migration des données                   | `V4`                                           | `data.DataMigrationTest`               |
| Une migration en PL/pgSQL, par lots        | `V6`                                           | `data.BatchedMigrationTest`            |
| Le rollback d'une migration                |                                                | `rollback.RollbackTest`                |
| Le roll forward                            |                                                | `rollback.RollForwardTest`             |
| Le changement rétrocompatible, les verrous |                                                | `deployment.BackwardCompatibilityTest` |
| Le renommage d'une colonne                 | `V7`, `V8`                                     | `deployment.ExpandContractTest`        |
| Le renommage d'une table avec une vue      |                                                | `deployment.RenameTableWithViewTest`   |
| La fusion de deux tables                   |                                                | `deployment.MergeTablesTest`           |
| Flyway au démarrage de Helidon             | `application.Migrations`, `Stage`, `Stages`    | `application.ApplicationTest`          |
| Les requêtes typées de jOOQ                | `jooq.StageReport`                             | `jooq.StageReportTest`                 |
| La migration qui casse la compilation      | `jooq.StageReport`                             | `jooq.StageReportTest`                 |

Une ligne sans code ne montre que du SQL : le test l'envoie directement à PostgreSQL. Les tests
de déploiement jouent l'ancien et le nouveau code par les requêtes que chacun enverrait.

## Ce qui sert à faire tourner les exemples

- **Deux bases.** Les tests vident la base `festival` et la migrent eux-mêmes. L'application
  Helidon garde la base `application`, car Flyway la migre à son démarrage.
- **Une base de build.** La génération de jOOQ lit une vraie base. Avant la compilation,
  `docker-maven-plugin` la démarre, `flyway-maven-plugin` lui applique les scripts, et
  `jooq-codegen-maven` la lit. Flyway s'arrête à `V3`, car `StageReport` lit la colonne `name`,
  que `V8` retire. Les classes générées arrivent dans `target/generated-sources/jooq`.
- **Des scripts copiés.** Un test qui modifie un script le fait sur une copie, dans un dossier
  temporaire : `Postgres.copyScripts` et `Postgres.flyway`.
- **Deux fichiers d'unités de persistance.** `persistence.xml` ne contient que l'unité JTA de
  l'application, car Helidon prend chaque unité de ce fichier pour la sienne. Les tests qui
  utilisent Hibernate seul lisent leurs unités dans
  `src/test/resources/META-INF/persistence-resource-local.xml`.

## Ce que le module laisse de côté

- La commande `undo` de Flyway, réservée à ses éditions payantes, et Liquibase.
- Un vrai déploiement progressif, avec deux versions de l'application qui tournent en même temps.
- Une copie par lots dans le script `V7`. Sur une grande table, elle suivrait le modèle de `V6`.
