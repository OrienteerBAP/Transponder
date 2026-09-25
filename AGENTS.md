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

| Area | Current (Stage A) |
|---|---|
| Java | `--release 21` (`maven.compiler.release`, `-parameters`), built/tested on JDK 21 **and** 25 |
| Core | ByteBuddy 1.18.14 (reads/writes Java 25 class files), Objenesis 3.2, Guava 33.4.8, Lombok 1.18.48 `provided` (annotation processor path) |
| OrientDB driver | `orientdb-core` 3.2.56 `provided`; its GraalVM 21.3.5 is excluded, GraalJS 25.0.4 in `test` scope (plan D4) |
| Other drivers | ArcadeDB 23.12.1 (+ GraalJS 25.0.4, D15), MongoDB driver 5.2.1; parked: Neo4j 4.4.38, JanusGraph 1.1.0 |
| Tests | JUnit Jupiter + vintage 5.14.4 (`junit-bom`), Hamcrest 3.0 |
| Build | Maven Wrapper 3.9.16; every plugin version pinned once in the root `<pluginManagement>`; enforcer: Maven ≥ 3.9, JDK ≥ 21 |

## Build status — read first

- **Stage A done (2026-09-25):** `./mvnw clean verify` green on Temurin 21.0.11 and 25.0.4 (112 tests, 0 failures, no
  `--add-opens`); `./mvnw install` puts `1.1-SNAPSHOT` (parent, core + test-jar, orientdb, arcadedb, mongodb) into `~/.m2`.
  Default reactor: parent, core, orientdb, arcadedb, mongodb. **Parked** (opt-in profile `parked`, never published):
  neo4j (Neo4j 4.4 can't run on JDK 21, D13) and janusgraph (6 failing tests, D14). `./mvnw -Pparked verify` tries them.
- **Stage B/C need no code** (no Wicket/servlet/`javax.*`). Never install or publish post-Stage-A work as `1.1-SNAPSHOT`:
  `1.1` is released after Orienteer P3 is green, then `master` → `1.2-SNAPSHOT` (plan D2).
- JDK 8 is gone: don't support it. `.sdkmanrc` pins Temurin 21.0.11; Temurin 25.0.4 for verification:
  `JAVA_HOME=~/.sdkman/candidates/java/25.0.4-tem ./mvnw clean verify`. JDK 27 is non-LTS.
- CI: `.github/workflows/ci.yml` (push + PR, JDK 21/25, default reactor, surefire reports). No publishing in CI.
- Publishing: OSSRH (oss.sonatype.org) and jcenter are dead; the Central Portal setup is phase R. **Only the owner runs
  `deploy`/releases**; agents never upload, push or tag. The `org.orienteer` namespace status is unconfirmed (plan D10).

## Commands

```bash
./mvnw clean verify                                  # build + tests + checkstyle (verify phase, fails the build)
./mvnw install                                       # parent pom + jars (+ core test-jar) into ~/.m2 for Orienteer
./mvnw -pl transponder-orientdb -am test             # one driver and what it needs
./mvnw -pl transponder-core test -Dtest=CoreSpecificTest   # single test class
```

## Layout and key classes

- `transponder-core/` — `org.orienteer.transponder`: `Transponder` (API: `define`, `create`, `provide`, `wrap`, `dao`,
  `delegate`, static `unwrap`/`rewrap`/`upgrade`/`save`), `IDriver` (driver SPI), `IMutator` + `mutator/*` (ByteBuddy
  builders for getters, setters, `@Query`, `@Lookup`, `@Command`, delegation, advice annotations), `BuilderScheduler`,
  `ProxyType`, `CommonUtils`, `annotation/*`, `polyglot/DefaultPolyglot` (per-dialect queries from
  `/META-INF/transponder/polyglot.properties`). Publishes a **test-jar** with the driver-independent test suite.
- `transponder-orientdb/` — `ODriver`, `IODocumentWrapper`, `@OrientDBProperty`, `advice/SudoAdvice` (`@Sudo`).
- `transponder-arcadedb/`, `-mongodb/` — other drivers; `-neo4j/`, `-janusgraph/` — parked drivers. None is used by Orienteer.
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
