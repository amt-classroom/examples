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

- `4-jdbc-and-jpa` (deck `4-jdbc-and-jpa.tsx`): JDBC (drivers, connections, the pool and its
  starvation), JPA (the entity, the `EntityManager`), then manipulating and querying data
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

Add one here, with its deck and sections, before starting it.
