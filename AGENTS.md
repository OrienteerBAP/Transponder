# Transponder

Object mapping ("Hibernate for NoSQL") library: entity and DAO **interfaces** annotated with `@EntityType`, `@Query`,
`@Lookup`, `@Command`… are turned into classes at runtime with [ByteBuddy](https://bytebuddy.net), generated over the
native DB objects (no reflection at call time, no second copy of the data). A driver per database also creates the schema.
It is the DAO layer of [Orienteer](https://github.com/OrienteerBAP/Orienteer) (local checkout `~/Development/Orienteer`)
and is modernized in lockstep with it.
**Read [`REFRESH_PLAN.md`](REFRESH_PLAN.md) before touching dependencies, build or infra; tick items off there in the same commit.**

## Contract with Orienteer — don't break it

- Coordinates `org.orienteer.transponder:transponder-orientdb` and `transponder-core`, parent `transponder-parent`,
  version **`1.1-SNAPSHOT`**. Never change groupId/artifactId/version without the owner.
- Orienteer's `org.orienteer.core.dao`: `DAO` creates the `Transponder`; `OrienteerDriver extends ODriver` and overrides
  `createType`, `onPostCreateType`, `createProperty`, `setupRelationship`, `getEntityMainClass`. 48 Orienteer files use
  the annotations, `OrientDBProperty`, `IODocumentWrapper`, `Transponder.rewrap`, `CommonUtils.listDeclaredMethods`.
- Public API = public/protected members of `transponder-core` and `transponder-orientdb`. Avoid signature changes; if one
  is unavoidable, list it in the plan's "Handoff to Orienteer".
- Stages: A → Orienteer P3 (JDK 21). B/C (Wicket 9/10, jakarta) need nothing here: no servlet/Wicket/`javax.*` coupling.

## Stack

| Area | Now | Target (plan) |
|---|---|---|
| Java | `release` 8 (arcadedb/neo4j/janusgraph: 11) | `--release 21`, verified on JDK 21 **and** 25 (Stage A) |
| Core | ByteBuddy 1.15.10, Objenesis 3.2, Guava 33.4, Lombok 1.18.34 `provided` | ByteBuddy 1.18.x (Java 25), Lombok 1.18.48 |
| OrientDB driver | `orientdb-core` 3.2.36 `provided` | 3.2.56; GraalJS 25.0.4 replaces OrientDB's Graal 21.3.5 in tests |
| Other drivers | ArcadeDB 23.12, Neo4j 4.4 (embedded), JanusGraph 1.1 + TinkerPop 3.7, MongoDB driver 5.2 | same majors; park if red (plan D7) |
| Tests | JUnit Jupiter + vintage 5.11, Hamcrest 3 | Jupiter + vintage 5.14.4 |

## Build status — read first

- Baseline (2026-09-25, plan Appendix C): on JDK 21 core, orientdb, arcadedb and mongodb are green; neo4j can't start
  its DB on JDK 21; janusgraph has 6 failing tests and 2 Checkstyle violations. On JDK 25 core doesn't even compile.
  Stage A (plan section A) fixes this; drivers that stay red move to the opt-in profile `parked` (D7).
- JDK 8 is gone: don't support it. `.sdkmanrc` pins Temurin 21.0.11; Temurin 25.0.4 for verification:
  `JAVA_HOME=~/.sdkman/candidates/java/25.0.4-tem ./mvnw clean verify`. JDK 27 is non-LTS.
- Publishing: OSSRH (oss.sonatype.org) and jcenter are dead; the Central Portal setup is phase R. **Only the owner runs
  `deploy`/releases**; agents never upload, push or tag. The `org.orienteer` namespace status is unconfirmed (plan D10).

## Commands

```bash
./mvnw clean verify                                  # build + tests + checkstyle (verify phase, fails the build)
./mvnw install                                       # parent pom + jars (+ core test-jar) into ~/.m2 for Orienteer
./mvnw -pl transponder-orientdb -am test             # one driver and what it needs
./mvnw -pl transponder-core test -Dtest=CoreSpecificTest   # single test class
```
(Until the wrapper lands in Stage A1, use `mvn`.)

## Layout and key classes

- `transponder-core/` — `org.orienteer.transponder`: `Transponder` (API: `define`, `create`, `provide`, `wrap`, `dao`,
  `delegate`, static `unwrap`/`rewrap`/`upgrade`/`save`), `IDriver` (driver SPI), `IMutator` + `mutator/*` (ByteBuddy
  builders for getters, setters, `@Query`, `@Lookup`, `@Command`, delegation, advice annotations), `BuilderScheduler`,
  `ProxyType`, `CommonUtils`, `annotation/*`, `polyglot/DefaultPolyglot` (per-dialect queries from
  `/META-INF/transponder/polyglot.properties`). Publishes a **test-jar** with the driver-independent test suite.
- `transponder-orientdb/` — `ODriver`, `IODocumentWrapper`, `@OrientDBProperty`, `advice/SudoAdvice` (`@Sudo`).
- `transponder-arcadedb/`, `-neo4j/`, `-janusgraph/`, `-mongodb/` — other drivers; not used by Orienteer.
- Every module has its own `AGENTS.md`. `check_style.xml` — Checkstyle rules for main sources.

## Conventions

- Tabs in almost all Java files and poms: match the surrounding file. No reformat-only diffs.
- Checkstyle fails the build on: missing Javadoc on public types/methods, missing `package-info.java`, interfaces not
  starting with `I`, lines > 200, anonymous classes > 60 lines, utility classes with a public constructor.
  Suppress with `//CHECKSTYLE IGNORE <Check> FOR NEXT <n> LINES` (used for `get$transponder`).
- `-parameters` is required: `@Query`/`@Command` parameters bind by Java parameter name.
- Lombok (`provided`) is used sparingly (`@UtilityClass`, `@Value`); don't spread it. No logging framework in main code.
- ByteBuddy and Guava types appear in public signatures (`IMutator`, `BuilderScheduler`, `ElementMatcher`).

## Testing

- JUnit 5 Jupiter (+ vintage for the JUnit 4 asserts in the core test-jar and `ArcadeDBUniversalTest`), Hamcrest 3.
- `AbstractUniversalTest` (core test-jar, 20 tests) runs against every driver through an `ITestDriver`
  (`TestDriver`, `OTestDriver`, `ArcadeDBTestDriver`, …); each driver has a `*UniversalTest` subclass.
- DBs are embedded and in-process: OrientDB `embedded:target/` memory DBs, ArcadeDB/Neo4j files under `target/db`,
  JanusGraph `inmemory`, MongoDB via flapdoodle (downloads a `mongod` binary once). No fixed ports.
- Driver-specific queries in tests come from `src/test/resources/META-INF/transponder/polyglot.properties`.

## Pitfalls

- Generated classes are cached per class loader in a static `TypeCache`; `setPolyglot` clears it.
- `CommonUtils.listDeclaredMethods` reads the interface's `.class` with ByteBuddy's repackaged ASM to get source order —
  property order in the schema depends on it.
- OrientDB 3.2.x brings GraalVM 21.3.5, which crashes embedded OrientDB on JDK 22+ (plan D4).
- Orienteer must pin ByteBuddy: its Mockito 2.22 (and later Wicket 9) bring older versions that win by nearest (plan D6).
- Keep `README.md` and the `AGENTS.md` files current when behavior, versions or commands change.
