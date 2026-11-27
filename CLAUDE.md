# CLAUDE.md

Guidance for agents (and people) working in the AMT slide exemples.

## What this repository is

Every deck of `../site/apps/web/src/decks/` illustrates its mechanisms on small, self-contained
Java examples, shown as code blocks on the slides. This repository holds the same code as
real, compiled, tested Maven modules, so a slide's snippet and its printed output are verified
against a real build instead of trusted by eye.

This is not `../exercises/`, the labs' correction, held back until after the lab. What lives
here is already public: it is exactly what the deck shows. **How this repository relates to
the deck and the lab is decided in full at `../exercises/AGENTS.md`** (the three roles: slide
example, lab spec, correction) — this file only adds what changes because the examples here
are published with the deck, not withheld.

**The deck is the spec for what a module contains.** A module mirrors one deck; each package
in it mirrors one section (one mechanism) of that deck. The code in a package and the code on
its slide must read as the same example: same class names, same method bodies, same printed
output. When they drift, fix whichever is wrong, in the same pass — usually the deck, since
this repository is where the example actually runs.

**Students read these modules on their own**, when a lecture was unclear, so every
student-facing file stands alone: READMEs, Javadoc, comments and `pom.xml` descriptions never
cite the slides. No slide title in quotes, no "deck N", "cours N", "partie N", "les notes",
"l'exercice", "comme sur la slide", and no link to `../exercises/`. Describe the mechanism
itself. This file is the one place that talks about the decks.

**These examples never touch the lab's problem.** A deck illustrates each mechanism on its own
example, never on the lab's problem (see `../exercises/AGENTS.md`, "Slides illustrate each
mechanism..."). A module here keeps the same rule: it must not reuse a class, package or
scenario name from `../exercises/`.

## Validate

```bash
./mvnw -q verify                                        # compile (-Xlint:all -Werror), enforcer, tests: must pass
./mvnw -q -pl 2-advanced-java-features -am verify        # one module and those it needs
```

Always use `./mvnw`, never a system `mvn`: the wrapper pins the Maven version the enforcer
requires. A change is done when `./mvnw verify` passes.

## Conventions

- One module per deck, named after its file in `../site/apps/web/src/decks/` without the
  extension (`2-advanced-java-features.tsx` → `2-advanced-java-features`). One package per
  slide section inside it, named after the mechanism it shows (`classloaders`, `reflection`,
  `annotations`, `proxies`).
- A section gets its own sibling module, suffixed `-processor`, only when the build forces it:
  an annotation processor must be compiled before the code it processes, exactly as
  `../exercises/01-command-line-processor` is split from `01-command-line`. Code that needs a
  running framework stays in the deck's module: deck 5 runs its Helidon application and its
  framework-free mechanics side by side.
- Base package `ch.heigvd.amt`.
- Java 21 (`maven.compiler.release`). No dependencies beyond the JDK and JUnit 5 unless the
  slide's mechanism needs them. Versions are managed in the root `pom.xml` only.
- Each printed or observed result a slide shows (an output, a boolean, a compiler error) has a
  test that reproduces it, as long as the test also reads as an example of the mechanism. A
  slide's "piège" (trap) is a test too, not just a comment. A check that only proves a claim
  from the speaker notes, such as a framework setting, stays out: students read these tests.
- READMEs, Javadoc, comments and `pom.xml` descriptions are written in French, like the decks;
  follow the Elements of Style rules (short, active, no em dashes). Identifiers, test names and
  messages the program prints stay in English, as the slides show them.
- Each module has a `README.md`: what it shows, one line per notion with its code and test, how
  to run it, and what it deliberately leaves out. It does not name the deck or its slides.

## Design principles

Same principles as `../exercises/AGENTS.md`, because a slide's reader has even less time than a
lab's: a mechanism must read at a glance, not be studied. Complexity is the enemy; the code is
educational, not production-ready, and leaves out the hardening the concept does not need.

- **Minimize complexity.** Count each field, flag, branch and type as a cost.
- **Prefer deep modules.** A small API hiding real work behind it.
- **Pull complexity downward.** Keep call sites declarative; the mechanism does the work.
- **One purpose per abstraction.** If a class needs more than one sentence to describe, split it.
- **Optimize for read time.** Plain names from the domain, straight-line flow, no cleverness.
- **Comments capture the why**, never the what.

## Adding an exemple

1. Read the deck section and copy the code it shows.
2. If the module for that deck does not exist yet, create it (`NN-slug/`, `pom.xml` whose
   parent is `amt-exemples`), and add it to the root `<modules>`.
3. Transcribe the slide's example into a package named after the section; keep names and
   output identical to what the slide states.
4. Write a test for every printed or observed result the slide shows, including its "pièges".
5. If the code does not compile or does not produce the output the slide claims, fix the
   deck (`../site/apps/web/src/decks/<slug>.tsx`) in the same pass.
6. Write the module README, add it to the table in the root `README.md`, run `./mvnw verify`.

## Planned exemples

- `4-jdbc-and-jpa` (deck `4-jdbc-and-jpa.tsx`): JDBC (drivers, connections, SQL injection and
  prepared statements, the pool and its starvation), JPA (the entity, the `EntityManager`), then manipulating and querying data
  (default mappings, the owning side, the lifecycle, flushing, JPQL, named queries, Criteria,
  N+1, fetching, the lost update). Its tests run on a real PostgreSQL started by
  Testcontainers, so `./mvnw verify` needs Docker.
- `5-transactions-and-validation` (deck `5-transactions-and-validation.tsx`): transactions
  (autocommit, JDBC, `@Transactional` and checked exceptions), isolation (anomalies under
  each PostgreSQL level, write skew), locking (pessimistic, optimistic, atomic update, unique
  constraint), timeouts and deadlocks, bulk writes (batches, flush and clear, `COPY`),
  prepared transactions, then Jakarta Validation (constraints, method validation, JPA
  integration, `CHECK`). Same
  Testcontainers setup. One module holds both the mechanics without a framework (JDBC, SQL,
  Hibernate on its own) and the services the slides show in Helidon MP 4.5 (JTA with
  Narayana, the JTA persistence unit, Bean Validation through REST, XA across two databases),
  tested with `@HelidonTest`. Helidon claims every unit in `META-INF/persistence.xml`, so
  that file holds only the application's JTA unit; the hand-made `RESOURCE_LOCAL` units live
  in `src/test/resources/META-INF/persistence-resource-local.xml`, which the test helper
  hands to Hibernate's `Bootstrap`.
- `6-database-evolution-and-migration` (deck `6-database-evolution-and-migration.tsx`): schema
  generation modes, Flyway (scripts, history, immutability), data migrations (`V4`, the batched
  `V6`), rollback and roll forward, backward compatibility, expand and contract (the column
  rename `V7`/`V8`, a table renamed behind a view, two tables merged), a Helidon MP application
  that runs Flyway at startup before Hibernate validates, then jOOQ's typed query and the
  compile error after a rename. The scripts in `src/main/resources/db/migration` are the ones the
  deck shows: **their exact text fixes the checksums on its slides**, and `FlywayTest` and
  `ImmutabilityTest` pin those values. Helidon has no Flyway extension, so `application.Migrations`
  is a CDI observer; without its `PLATFORM_BEFORE` priority Hibernate starts first and rejects
  the empty database. `./mvnw compile` needs Docker here: `docker-maven-plugin` starts a build
  database at `initialize`, then Flyway (`target` 3, because `StageReport` reads `stage.name`) and
  jOOQ's generator run at `generate-sources`. The timings on the batched slide are measured by
  `BatchedMigrationTest` and vary from run to run; its output is in
  `target/surefire-reports/ch.heigvd.amt.data.BatchedMigrationTest-output.txt`.
- `7-dependency-inversion-and-injection` (deck `7-dependency-inversion-and-injection.tsx`): the
  dependency inversion principle (`inversion.direct` against `inversion`), injection by setter
  and by the container, inversion of control (a Jakarta REST resource called over HTTP),
  Jakarta CDI one package per notion (injection points, scopes and the client proxy, ambiguity,
  `@Named`, custom qualifiers, alternatives, producers, `Instance`, lifecycle, interceptors,
  decorators, events) plus a hand-written `singleton`, the solution of the deck's closing exercise (`library`: a publisher, a
  shelf, a librarian and a manager, added at the user's request on 2026-11-20; it is a deck
  exercise, not a lab), then test doubles with Mockito (`doubles`) and tests inside the
  container (`container`). The `inversion`, `injection` and `control` packages use a bare
  `Application` / `Logger` / `ConsoleLogger` example, as the slides do, so that no class name
  carries a role (the user asked for it on 2026-11-20: a `ReservationService` in the client
  role confused the roles); the festival beans the CDI tests share (`ReservationService`,
  `SmtpMailer`) are in `festival`. Helidon MP 4.5, no database, no Docker. Simplified to the minimum on
  2026-11-20, at the user's request: one or two tests per notion, and only the traps a slide
  states (the forgotten setter, the `@Named` typo). What only the speaker notes claim stays out. Later the same day
  the user cut the deck's details, and the module with them: asynchronous and qualified events, `@MockBean`, and all of Mockito beyond `mock`,
  `when` and `verify` (annotations, matchers, verification modes, stubbed exceptions, spies).
  Do not add them back unless the deck does. Setter injection and the `Instance` lookup were cut
  too and restored at once: the user judged that went too far. The module holds several variants of the
  same bean, so **no test may rely on bean discovery**: every `@HelidonTest` carries
  `@DisableDiscovery` and names its beans with `@AddBean`, and there is no `beans.xml`.
  `@AddBean` forces `@ApplicationScoped` unless told otherwise, which an interceptor or a
  decorator rejects (`scope = Dependent.class`). `Container.start` boots Weld by hand for what
  `@HelidonTest` cannot show: a startup the container refuses and its shutdown.
- `8-presentation-serialization-communication` (deck
  `8-presentation-serialization-communication.tsx`, the former deck 10 redone on Helidon at the
  user's request on 2026-11-27): the presentation layer (`presentation`: a `Concert` with a fee
  the `ConcertDto` leaves out, a mapper, a Jakarta REST resource and its status codes, tested
  through the `WebTarget` of `@HelidonTest`), Java serialization (`serialization`, with
  `serialization.external` for `Externalizable`), XML (`xml`: JAXB and a resource that produces
  XML), JSON (`json`: the Jackson `ContextResolver` that writes ISO 8601 dates), Protobuf
  (`src/main/proto/concert.proto`, sizes of one concert in four formats) and gRPC (`grpc`).
  Helidon MP 4.5, no database, no Docker; bean discovery is on (`beans.xml`), one application.
  **Surefire forks one JVM per test class** (`reuseForks=false`): once Helidon has started, its
  JVM-wide filter rejects every Java deserialization, which `HelidonFilterTest` shows and which
  would break the plain serialization tests. The byte counts on the slides (106, 76, and 197 /
  184 / 92 / 55) are pinned by tests and depend on class and package names: renaming one changes
  them, and the deck with it. The gRPC service uses Helidon's `@Grpc` annotations, not the code
  `protoc-gen-grpc-java` generates: that plugin ships an x86_64 binary under its `osx-aarch_64`
  classifier, which fails on an Apple silicon Mac without Rosetta. The gRPC method returns its
  message directly; an exception it throws reaches the client as `INTERNAL`, whatever its status
  (answering `NOT_FOUND` takes a `StreamObserver`, which the example leaves out on purpose).
  The deck's part on HTTP seen from the server was added the same day, because the students
  learn HTTP in a web course and the deck must not teach it again: `errors` (a business
  exception mapped to a 409 problem body), `idempotency` (a POST
  replayed with an `Idempotency-Key`), `conditional` (`ETag`, `If-Match`, 412), `session` (a
  `@SessionScoped` cart, kept on purpose as a trap: it has no context in Helidon and its
  resource answers 500) and the OpenAPI document served on `/openapi` (`ContractTest`). The
  `jandex-maven-plugin` is what lets that document describe the DTO fields. The stage-busy rule
  lives in `ConcertService.schedule`, so every test that posts a concert must pick a free slot.
  `xsd` and `xjc` bring back what the former deck's companion repository ran (the user missed
  XJC on 2026-11-27): `src/main/resources/schema/programme.xsd` describes the same document as
  the hand-written `xml.Programme`, `xsd.Validation` validates against it, and
  `jaxb2-maven-plugin` generates `ch.heigvd.amt.xjc.generated`. The validator translates its
  messages into the JVM's language, so `ValidationTest` sets the default locale to English.
  `parsers` (DOM, SAX, StAX, also from that repository) reads one programme three ways.
  `ParsersTest` pins a trap the deck states on its XML limits slide: with default settings the
  three JDK parsers (checked on Java 25) resolve an external entity and print a local file;
  `SafeDom` refuses any `DOCTYPE`. Do not harden `Dom`, `Sax` or `Stax`: the trap needs them as
  they are.
  `rmi` (added 2026-11-27, when the user asked for a working RMI example on the deck's "Un appel
  RMI" slide; RMI is not deprecated, JEP 385 only deprecated RMI Activation): a `TicketOffice`
  remote interface, a `Server` and a `Client` that run in two JVMs and reuse
  `serialization.Ticket`. `RmiTest` starts `Server` as a separate process from `target/classes`
  and pins three results: the stub is a dynamic proxy; a call after the server died throws a
  `RemoteException` (assert the supertype: it is `ConnectException` or `UnmarshalException`
  depending on whether the client reuses a connection); an exported object nobody holds is
  garbage collected and the next call throws `NoSuchObjectException`. That last one is why
  `Server` keeps the office in a static field: do not inline it.
  `ech` (added 2026-11-27 for the deck's "Une déclaration d'impôt" slide, after the user asked for
  a slide on eCH and the banking XML standards, where parsers are generated and not hand-written):
  `src/main/resources/schema/ech/` holds unmodified copies of the eCH-0119 schema (tax return of
  a natural person) and the twelve eCH schemas it imports, downloaded from ech.ch with the
  user's agreement; eCH standards may be used and distributed freely if eCH is credited, which
  the directory's `README.md` does. Do not edit those files. A second `xjc` execution generates
  162 classes from them into `target/generated-sources/jaxb-ech`, in packages derived from the
  namespaces (`ch.ech.xmlns.ech_0119._4`); each execution has its own output directory because
  XJC clears it. eCH schemas import each other by their URL on ech.ch, or by namespace alone:
  `catalog.xml` maps both to the local copies so the build stays offline (checked with the
  network blocked). `EchTest` pins the counts on the slide (13 schemas, 162 classes).
  The deck's OpenAPI slide moved from the HTTP part to the JSON part on 2026-11-27 (the user's
  choice: it is JSON's schema, as the XSD is XML's); `ContractTest` and its package did not move.
  **Keep this module small.** On 2026-11-27 the user asked to simplify its examples as far as
  possible, so that students learn the concepts and are not lost in details. What went: the
  `StreamObserver` in the gRPC service, schema validation in `ech.Main` (it needed the thirteen
  schemas loaded in dependency order, because the JDK's catalog support throws a
  `NullPointerException` on an import without `schemaLocation`), and `errors.ValidationMapper`
  (it needed a `@Priority` to win over Helidon's own mapper, and the user had removed the notes
  that showed it). Before adding code to an example, check that a slide shows it.
- `tika-and-poi` (no deck: the user asked on 2026-11-27 for a standalone Apache Tika and Apache
  POI example, so the module is named after its libraries and not after a deck file): one
  package, `tables`. `PdfTables` is a SAX handler that collects the tables Tika 4 emits for
  `src/main/resources/report.pdf`; `ExcelWorkbook` writes one sheet per table with typed cells;
  `Main` writes `target/report.xlsx`. Tika only emits `table` elements because the PDF is
  tagged: it is `report.html` printed by headless Chrome (`--headless --no-pdf-header-footer
  --print-to-pdf`), so regenerate it that way and not with a tool that writes no tags. Tika
  splits the first table at the page break and drops empty cells; the handler merges the halves,
  and the report has no empty cell on purpose (no total row). Tika 4 takes a `TikaInputStream`,
  not an `InputStream`. `slf4j-simple` and `log4j-to-slf4j` only silence the two libraries'
  startup warnings.

Add one here, with its deck and sections, before starting it.
