# 8 - Présentation, sérialisation et communication

Des exemples exécutables sur la couche présentation d'une application, sur ce que le serveur
fait pour tenir les promesses de HTTP, et sur les formats qui traversent sa frontière : la
sérialisation Java, XML, JSON et Protobuf. Chaque exemple est un test JUnit.

Les exemples restent dans le festival : un `Concert` que la couche métier connaît, cachet de
l'artiste compris, et un `ConcertDto` que l'API publie, sans le cachet. Le `ConcertService`
garde le programme en mémoire : le module n'a pas de base de données.

L'application tourne dans Helidon MP 4.5, qui embarque Jersey pour Jakarta REST. Le JSON vient de
Jackson, le XML de JAXB.

## Exécuter

```bash
./mvnw -pl 8-presentation-serialization-communication verify
```

Le module n'a pas besoin de Docker. Au premier build, Maven télécharge `protoc`, le compilateur
de Protobuf, pour la machine qui compile. Le build génère aussi du code dans
`target/generated-sources` : les classes JAXB des schémas `programme.xsd` et eCH-0119, par XJC, et
les messages Protobuf.

## Les exemples

Les paquets sont sous `ch.heigvd.amt`.

### La couche présentation

| Notion                                   | Code                              | Test                  |
| ---------------------------------------- | --------------------------------- | --------------------- |
| Le DTO                                   | `presentation.ConcertDto`         | `ConcertResourceTest` |
| Le mapper                                | `presentation.ConcertMapper`      | `ConcertResourceTest` |
| La ressource Jakarta REST                | `presentation.ConcertResource`    | `ConcertResourceTest` |
| Les codes de statut : 200, 201, 400, 404 | `presentation.ConcertResource`    | `ConcertResourceTest` |
| Le test par HTTP                         | `@HelidonTest` et son `WebTarget` | `ConcertResourceTest` |

### HTTP vu du serveur

| Notion                                                | Code                              | Test              |
| ----------------------------------------------------- | --------------------------------- | ----------------- |
| L'erreur métier traduite en 409                       | `errors.StageBusyMapper`          | `ErrorsTest`      |
| La requête rejouée et sa clé d'idempotence            | `idempotency.ReservationResource` | `IdempotencyTest` |
| La modification concurrente : `ETag`, `If-Match`, 412 | `conditional.StageResource`       | `ConditionalTest` |
| Le bean `@SessionScoped`, sans session dans Helidon   | `session.Cart`                    | `SessionTest`     |

### La sérialisation Java

| Notion                                         | Code                                           | Test                 |
| ---------------------------------------------- | ---------------------------------------------- | -------------------- |
| `Serializable`, `transient`, l'aller-retour    | `serialization.Ticket`, `Main`                 | `SerializationTest`  |
| `Externalizable`                               | `serialization.external.Ticket`                | `ExternalizableTest` |
| Le filtre `ObjectInputFilter`                  | `serialization.Filter`                         | `SerializationTest`  |
| Le filtre que Helidon installe                 | `serialization.Main`                           | `HelidonFilterTest`  |
| Un appel RMI entre deux JVM                    | `rmi.TicketOffice`, `rmi.Server`, `rmi.Client` | `RmiTest`            |
| Le stub, un proxy créé à l'exécution           | `rmi.Client`                                   | `RmiTest`            |
| L'appel qui échoue quand le serveur a disparu  | `rmi.TicketOffice`                             | `RmiTest`            |
| L'objet exporté que le ramasse-miettes reprend | `rmi.Server`                                   | `RmiTest`            |

Pour voir l'appel RMI entre deux terminaux, après un build :

```bash
java -cp 8-presentation-serialization-communication/target/classes ch.heigvd.amt.rmi.Server
```

```bash
java -cp 8-presentation-serialization-communication/target/classes ch.heigvd.amt.rmi.Client
```

Le serveur écoute sur le port 1099 et tourne jusqu'à `Ctrl+C`. Le client affiche le billet reçu.

### XML

| Notion                                              | Code                                         | Test                    |
| --------------------------------------------------- | -------------------------------------------- | ----------------------- |
| Les parseurs DOM, SAX et StAX                       | `parsers.Dom`, `parsers.Sax`, `parsers.Stax` | `ParsersTest`           |
| L'entité externe que ces parseurs lisent par défaut | `parsers.Dom`                                | `ParsersTest`           |
| Le parseur qui refuse `DOCTYPE`                     | `parsers.SafeDom`                            | `ParsersTest`           |
| JAXB : marshal, unmarshal                           | `xml.Programme`                              | `JaxbTest`              |
| JAXB et Jakarta REST                                | `xml.ProgrammeResource`                      | `ProgrammeResourceTest` |
| La négociation de contenu                           | `xml.ProgrammeResource`                      | `ProgrammeResourceTest` |
| Le schéma XSD et la validation                      | `schema/programme.xsd`, `xsd.Validation`     | `ValidationTest`        |
| Les classes générées par XJC                        | `xjc.Main`, `xjc.generated.Programme`        | `XjcTest`               |
| Une déclaration d'impôt eCH-0119, lue sans parseur  | `ech.Main`, `schema/ech/`                    | `EchTest`               |

Les schémas de `schema/ech/` sont ceux de l'association eCH, copiés de son site : la norme
eCH-0119 de la déclaration d'impôt et les douze schémas qu'elle importe. Le `README.md` du dossier
dit d'où ils viennent.

### JSON

| Notion                                  | Code                          | Test                  |
| --------------------------------------- | ----------------------------- | --------------------- |
| Un record écrit et relu en JSON         | `presentation.ConcertDto`     | `JsonTest`            |
| Les dates : tableau ou ISO 8601         | `json.JacksonConfig`          | `JsonTest`            |
| JSON et Jakarta REST                    | `json.JacksonConfig`          | `ConcertResourceTest` |
| Le contrat OpenAPI servi sur `/openapi` | les ressources et leurs DTO   | `ContractTest`        |
| Le champ inconnu, refusé par Jackson    | `presentation.ConcertRequest` | `ContractTest`        |

### Protobuf et gRPC

| Notion                            | Code                           | Test                 |
| --------------------------------- | ------------------------------ | -------------------- |
| Le schéma et les classes générées | `src/main/proto/concert.proto` | `ProtobufTest`       |
| Un concert dans quatre formats    | `protobuf.Concert`             | `ProtobufTest`       |
| Un service gRPC dans Helidon      | `grpc.ConcertCatalog`          | `ConcertCatalogTest` |

## Ce qui sert à faire tourner les exemples

- **Une JVM par classe de test.** Une fois démarré, Helidon refuse toute désérialisation Java
  dans sa JVM. Les tests sans serveur du paquet `serialization` tournent donc à part.
- **L'index Jandex.** Un plugin Maven indexe les classes à la compilation. Helidon y lit les
  champs des DTO pour décrire l'API ; sans lui, le contrat les réduit à `object`.
- **Le serveur RMI du test.** `RmiTest` lance `rmi.Server` dans une autre JVM, sur un port libre,
  et l'arrête après chaque test. Il fixe `java.rmi.server.hostname` à `localhost` : sans cela, le
  stub porte l'adresse locale de la machine, et le test dépendrait de son réseau.
- **Le catalogue des schémas eCH.** Ces schémas se désignent par leur adresse sur ech.ch, ou par
  leur seul espace de noms. `schema/ech/catalog.xml` les fait correspondre aux copies du module :
  XJC ne touche pas au réseau.
- **`Output.printed`.** Il rend les lignes qu'un exemple écrit sur la sortie standard.
- **Le client gRPC du test.** `ConcertCatalogTest` décrit le service à la main avec le client de
  Helidon. Une application le génère depuis le fichier `.proto`.

## Ce que le module laisse de côté

- Une base de données : le programme, les réservations et leurs clés d'idempotence, la scène et
  sa version sont en mémoire. Une vraie application les garde dans la base : la clé sous une
  contrainte unique, la version dans un champ `@Version`.
- Le refus d'une réservation sans clé, et celui d'une modification sans `If-Match`.
- Les annotations OpenAPI qui précisent le contrat, comme `@APIResponse`.
- L'authentification : le jeton qui dit qui appelle.
- Les autres standards à schéma que le cours cite : eCH-0020 et les messages ISO 20022.
- Le durcissement des parseurs SAX et StAX contre les entités externes : seul DOM est montré.
- JSON-B, l'autre bibliothèque JSON que Helidon accepte.
- Le client et le serveur gRPC générés par `protoc`. Son plugin Java n'existe pas pour les Mac à
  puce Apple sans Rosetta : le service utilise les annotations de Helidon.
- Les appels gRPC en flux, et la réponse par un code de statut gRPC comme `NOT_FOUND` : les deux
  passent par un `StreamObserver`.
- Le corps d'erreur d'une requête invalide : Helidon répond son 400 par défaut.
- La validation de la déclaration d'impôt contre les schémas eCH.
- RMI sur un réseau : le registre et l'objet écoutent sur deux ports, sans chiffrement ni
  authentification. L'exemple reste sur une seule machine.
