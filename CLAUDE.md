# CLAUDE.md

Guidance for agents (and people) working in the AMT slide exemples.

## What this repository is

Every deck of `../site/apps/web/src/decks/` illustrates its mechanisms on small, self-contained
Java examples, shown as code blocks on the slides. This repository holds the same code as
real, compiled, tested Maven modules, so a slide's snippet and its printed output are verified
against a real build instead of trusted by eye.

This is not `../examples/`, the labs' correction, held back until after the lab. What lives
here is already public: it is exactly what the deck shows. **How this repository relates to
the deck and the lab is decided in full at `../examples/AGENTS.md`** (the three roles: slide
example, lab spec, correction) — this file only adds what changes because the examples here
are published with the deck, not withheld.

**The deck is the spec for what a module contains.** A module mirrors one deck; each package
in it mirrors one section (one mechanism) of that deck. The code in a package and the code on
its slide must read as the same example: same class names, same method bodies, same printed
output. When they drift, fix whichever is wrong, in the same pass — usually the deck, since
this repository is where the example actually runs.

**These examples never touch the lab's problem.** A deck illustrates each mechanism on its own
example, never on the lab's problem (see `../examples/AGENTS.md`, "Slides illustrate each
mechanism..."). A module here keeps the same rule: it must not reuse a class, package or
scenario name from `../examples/`.

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
  `../examples/01-command-line-processor` is split from `01-command-line`.
- Base package `ch.heigvd.amt`.
- Java 21 (`maven.compiler.release`). No dependencies beyond the JDK and JUnit 5 unless the
  slide's mechanism needs them. Versions are managed in the root `pom.xml` only.
- Each printed or observed result a slide shows (an output, a boolean, a compiler error) has a
  test that reproduces it. A slide's "piège" (trap) is a test too, not just a comment.
- READMEs, Javadoc, comments and `pom.xml` descriptions are written in French, like the decks;
  follow the Elements of Style rules (short, active, no em dashes). Identifiers, test names and
  messages the program prints stay in English, as the slides show them.
- Each module has a `README.md`: which deck it illustrates, one line per section/package, and
  what it deliberately leaves out.

## Design principles

Same principles as `../examples/AGENTS.md`, because a slide's reader has even less time than a
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

None yet beyond deck 2. Add one here, with its deck and sections, before starting it.
