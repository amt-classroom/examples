# 7 - Inversion et injection de dépendances

Des exemples exécutables sur l'inversion des dépendances, l'injection, Jakarta CDI et les
doublures de test. Chaque exemple est un test JUnit. Ils restent dans la billetterie d'un
festival : un `ReservationService` qui a besoin d'un `Mailer` pour confirmer une réservation, et
d'un `PaymentGateway` pour débiter l'acheteur.

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
| Les points d'injection              | `cdi.points`           | `InjectionPointsTest`    |
| Les portées et le proxy client      | `cdi.scopes`           | `ScopesTest`             |
| L'ambiguïté                         | `cdi.ambiguity`        | `AmbiguityTest`          |
| Le qualificateur `@Named`           | `cdi.named`            | `NamedTest`              |
| Le qualificateur personnalisé       | `cdi.qualifier`        | `QualifierTest`          |
| L'alternative                       | `cdi.alternative`      | `AlternativeTest`        |
| Le producteur                       | `cdi.producer`         | `ProducerTest`           |
| La recherche programmatique         | `cdi.lookup`           | `LookupTest`             |
| Le cycle de vie                     | `cdi.lifecycle`        | `LifecycleTest`          |
| L'intercepteur                      | `cdi.interceptor`      | `InterceptorTest`        |
| Le décorateur                       | `cdi.decorator`        | `DecoratorTest`          |
| L'événement et l'observateur        | `cdi.events`           | `EventsTest`             |
| L'événement asynchrone              | `cdi.events.async`     | `AsyncEventsTest`        |
| L'événement qualifié                | `cdi.events.qualified` | `QualifiedEventsTest`    |
| Les doublures de test, avec Mockito | `doubles`              | un test par notion       |
| Le test avec le conteneur           | `container`            | `ContainerTest`          |
| Le mock dans le conteneur           | `container`            | `MockBeanTest`           |

L'injection par le constructeur est celle du paquet `inversion`.

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
- Les portées `@SessionScoped` et `@Singleton`.
