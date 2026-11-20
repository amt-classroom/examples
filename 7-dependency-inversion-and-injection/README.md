# 7 - Inversion et injection de dépendances

Des exemples exécutables sur l'inversion des dépendances, l'injection, Jakarta CDI et les
doublures de test. Chaque exemple est un test JUnit.

Les paquets `inversion`, `injection` et `control` montrent les rôles sur trois types sans
métier : une `Application` qui a besoin d'un `Logger`, et un `ConsoleLogger` qui le fournit. Les
autres restent dans la billetterie d'un festival : un `ReservationService` qui a besoin d'un
`Mailer` pour confirmer une réservation, et d'un `PaymentGateway` pour débiter l'acheteur.

Les beans CDI tournent dans Helidon MP 4.5, dont le conteneur est Weld. Les doublures viennent de
Mockito.

## Exécuter

```bash
./mvnw -pl 7-dependency-inversion-and-injection verify
```

Le module n'a besoin ni de Docker ni d'une base de données, et tourne en quelques secondes.

## Les exemples

Les paquets sont sous `ch.heigvd.amt`. Chaque paquet contient une notion et son test. Le paquet
`festival` contient les types qu'ils partagent.

| Notion                              | Paquet                 | Test                     |
| ----------------------------------- | ---------------------- | ------------------------ |
| La dépendance directe               | `inversion.direct`     | `DirectDependencyTest`   |
| L'inversion des dépendances         | `inversion`            | `InversionTest`          |
| L'injection par le setter           | `injection.setter`     | `SetterInjectionTest`    |
| L'injection par le conteneur        | `injection.container`  | `ContainerInjectionTest` |
| L'inversion de contrôle             | `control`              | `InversionOfControlTest` |
| L'injection avec `@Inject`          | `cdi.points`           | `InjectionPointsTest`    |
| Le scope et le client proxy         | `cdi.scopes`           | `ScopesTest`             |
| La dépendance ambiguë               | `cdi.ambiguity`        | `AmbiguityTest`          |
| Le qualifier `@Named`               | `cdi.named`            | `NamedTest`              |
| Le qualifier personnalisé           | `cdi.qualifier`        | `QualifierTest`          |
| L'alternative                       | `cdi.alternative`      | `AlternativeTest`        |
| Le producer `@Produces`             | `cdi.producer`         | `ProducerTest`           |
| Le lookup avec `Instance`           | `cdi.lookup`           | `LookupTest`             |
| Les callbacks du cycle de vie       | `cdi.lifecycle`        | `LifecycleTest`          |
| L'interceptor                       | `cdi.interceptor`      | `InterceptorTest`        |
| Le decorator                        | `cdi.decorator`        | `DecoratorTest`          |
| L'event et l'observer               | `cdi.events`           | `EventsTest`             |
| L'event asynchrone                  | `cdi.events.async`     | `AsyncEventsTest`        |
| L'event qualifié                    | `cdi.events.qualified` | `QualifiedEventsTest`    |
| Une bibliothèque qui réunit CDI     | `library`              | `LibraryTest`            |
| Les doublures de test, avec Mockito | `doubles`              | un test par notion       |
| Le test avec le conteneur           | `container`            | `ContainerTest`          |
| Le mock dans le conteneur           | `container`            | `MockBeanTest`           |

L'injection par le constructeur est celle du paquet `inversion`.

Le paquet `library` assemble les notions de CDI dans une petite bibliothèque. Un `Publisher`
produit deux livres, chacun avec son qualifier. L'étagère `LibraryShelf` reçoit ceux de fantasy
à sa création. Le `Librarian` prête et reprend les livres. Le decorator `StampingShelf` marque
prêté chaque livre qui quitte l'étagère, l'interceptor `BellInterceptor` sonne à chaque prêt et
à chaque retour, et le `Manager` observe l'event que le bibliothécaire émet à chaque retour.

## Ce qui sert à faire tourner les exemples

- **`@DisableDiscovery` et `@AddBean`.** Le module contient plusieurs variantes d'un même bean,
  par exemple trois `SmtpMailer`. Chaque test `@HelidonTest` nomme donc les beans de son
  conteneur. Une vraie application laisse le conteneur les découvrir, avec un fichier
  `META-INF/beans.xml`.
- **`Container.start`.** Il démarre Weld avec quelques classes, pour les tests qui observent un
  démarrage refusé ou l'arrêt du conteneur.
- **`Output.printed`.** Il rend les lignes qu'un exemple écrit sur la sortie standard.

## Ce que le module laisse de côté

- Un vrai serveur SMTP et un vrai moyen de paiement.
- Les scopes `@SessionScoped` et `@Singleton`.
