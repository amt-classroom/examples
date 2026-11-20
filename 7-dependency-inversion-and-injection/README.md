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

### Les rôles, sans conteneur puis avec

| Notion                       | Paquet                | Test                     |
| ---------------------------- | --------------------- | ------------------------ |
| La dépendance directe        | `inversion.direct`    | `DirectDependencyTest`   |
| L'inversion des dépendances  | `inversion`           | `InversionTest`          |
| L'injection par le setter    | `injection.setter`    | `SetterInjectionTest`    |
| L'injection par le conteneur | `injection.container` | `ContainerInjectionTest` |
| L'inversion de contrôle      | `control`             | `InversionOfControlTest` |

L'injection par le constructeur est celle du paquet `inversion`.

### Le conteneur CDI

| Notion                        | Paquet          | Test                  |
| ----------------------------- | --------------- | --------------------- |
| L'injection avec `@Inject`    | `cdi.points`    | `InjectionPointsTest` |
| Le scope                      | `cdi.scopes`    | `ScopesTest`          |
| Les callbacks du cycle de vie | `cdi.lifecycle` | `LifecycleTest`       |

### Les design patterns que CDI fournit

Chaque mécanisme de CDI réalise un design pattern : le conteneur fournit la mécanique, les
beans tiennent les rôles.

| Pattern     | Notion                        | Paquet                 | Test                  |
| ----------- | ----------------------------- | ---------------------- | --------------------- |
| Singleton   | Le singleton écrit à la main  | `singleton`            | `SingletonTest`       |
| Proxy       | Le client proxy               | `cdi.scopes`           | `ScopesTest`          |
| Strategy    | La dépendance ambiguë         | `cdi.ambiguity`        | `AmbiguityTest`       |
| Strategy    | Le qualifier `@Named`         | `cdi.named`            | `NamedTest`           |
| Strategy    | Le qualifier personnalisé     | `cdi.qualifier`        | `QualifierTest`       |
| Strategy    | L'alternative                 | `cdi.alternative`      | `AlternativeTest`     |
| Strategy    | Le lookup avec `Instance`     | `cdi.lookup`           | `LookupTest`          |
| Factory     | Le producer `@Produces`       | `cdi.producer`         | `ProducerTest`        |
| Interceptor | L'interceptor                 | `cdi.interceptor`      | `InterceptorTest`     |
| Decorator   | Le decorator                  | `cdi.decorator`        | `DecoratorTest`       |
| Observer    | L'event et l'observer         | `cdi.events`           | `EventsTest`          |

Le singleton que le conteneur tient à notre place est le bean `@ApplicationScoped` du paquet
`injection.container`.

Le paquet `library` réunit ces patterns dans une petite bibliothèque, testée par `LibraryTest`.
Un `Publisher` produit deux livres, chacun avec son qualifier. L'étagère `LibraryShelf` reçoit
ceux de fantasy à sa création. Le `Librarian` prête et reprend les livres. Le decorator `StampingShelf` marque
prêté chaque livre qui quitte l'étagère, l'interceptor `BellInterceptor` sonne à chaque prêt et
à chaque retour, et le `Manager` observe l'event que le bibliothécaire émet à chaque retour.

### Les tests

| Notion                              | Paquet      | Test               |
| ----------------------------------- | ----------- | ------------------ |
| Le stub                             | `doubles`   | `StubTest`         |
| Le mock                             | `doubles`   | `MockTest`         |
| Le fake                             | `doubles`   | `FakeTest`         |
| Une doublure faite par Mockito      | `doubles`   | `MockitoTest`      |
| Le test avec le conteneur           | `container` | `ContainerTest`    |

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
- Les events asynchrones et les events qualifiés.
- Le reste de Mockito : annotations, matchers d'arguments, nombre et ordre des appels,
  exceptions simulées, spies, et `@MockBean` dans le conteneur.
