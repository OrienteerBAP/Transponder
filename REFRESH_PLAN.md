# Transponder Refresh Plan

Living plan for bringing Transponder from its 2025 state (Java 8/11 targets, ByteBuddy 1.15, OrientDB 3.2.36, OSSRH
publishing) to current, supported versions, in lockstep with
[Orienteer's plan](https://github.com/OrienteerBAP/Orienteer/blob/master/REFRESH_PLAN.md) (local:
`~/Development/Orienteer/REFRESH_PLAN.md`). **Tick items off (`[ ]` → `[x]`) in the same commit that completes them**,
and add a short note (date, deviations) after the item when useful. Module details: each module's `AGENTS.md`.

Started: 2026-09-25. Versions below were checked on Maven Central on that date — re-check at the start of every stage
(`./mvnw versions:display-dependency-updates versions:display-plugin-updates`).

## Progress overview

| Phase | Goal | Depends on | Unblocks Orienteer | Status |
|---|---|---|---|---|
| P0 | AI initialization: AGENTS.md files + this plan | — | — | done |
| A | Stage A: JDK 21/25 build with `--release 21` on the **same** majors (OrientDB 3.2.x, ByteBuddy 1.x, JUnit 5), `./mvnw install` as `1.1-SNAPSHOT` | P0 | P3 | done (local install; published artifact → R) |
| B | Stage B (Orienteer P5, Wicket 9): no Wicket/servlet coupling → verification only | A | P5 | todo (expected: nothing to change) |
| C | Stage C (Orienteer P6, jakarta): no `javax.*` → verification only | B | P6 | todo (expected: nothing to change) |
| R | Release & CI: GitHub Actions, Maven Central via the Central Portal: `1.1-SNAPSHOT` now, `1.1` after Orienteer P3 is green (D2/D10) | A | P3 "resolvable from a real repo" | todo |
| X | Cleanup, docs, parked drivers, known bugs | any time | — | todo |

## Guiding principles

1. **Never break the Orienteer contract** (below): coordinates, version `1.1-SNAPSHOT`/`1.1` (= Stage A), public API of
   `transponder-core` and `transponder-orientdb` (Orienteer's `OrienteerDriver extends ODriver`).
2. **Keep the build green after every item.** One concern per commit; no big-bang upgrades.
3. **Same majors in Stage A:** JDK/tooling and patch/minor bumps only. DB majors of the other drivers are not ported (D7).
4. **Minimal, behavior-preserving code changes.** Any unavoidable public API change goes to "Handoff to Orienteer".
5. **No reformat-only diffs;** match the surrounding indentation (tabs almost everywhere).

## Decisions log

| # | Date | Decision | Status |
|---|---|---|---|
| D1 | 2026-09-25 | Target `--release 21`; verify on JDK 21 **and** 25 (Temurin 21.0.11 / 25.0.4 via SDKMAN). JDK 8/11/17 support is dropped (the README's Java 8+ claims go). JDK 27 (non-LTS) is not targeted. Same as Orienteer D1. | accepted |
| D2 | 2026-09-25 | Coordinates stay `org.orienteer.transponder:transponder-{parent,core,orientdb,…}`, version **`1.1-SNAPSHOT`** for Stage A. Per Orienteer D9: `1.1-SNAPSHOT` is published to the Central Portal snapshots repository; `1.1` is released only after Orienteer's P3 build is green against it (fixes go into the snapshot first); then `master` moves to `1.2-SNAPSHOT`. Stage B/C work (if any) is never installed or published as `1.1-SNAPSHOT`; an API-breaking Stage C would be `2.0`. | accepted (Orienteer D9) |
| D3 | 2026-09-25 | `transponder-orientdb` compiles and tests against **OrientDB 3.2.56** (Orienteer contract row). `orientdb-core` stays `provided`: Orienteer's `dependencyManagement` decides the runtime version. | accepted |
| D4 | 2026-09-25 | **GraalJS swap, test scope only** (Orienteer D8): exclude `org.graalvm.{sdk,truffle,js,tools}` from `orientdb-core` and add GraalJS 25.0.4 (`polyglot`, `js-scriptengine`, `js` pom) with `test` scope. Since `orientdb-core` is `provided`, compile scope would push GraalJS onto Orienteer for nothing (Orienteer gets it from wicket-orientdb). | accepted (owner, 2026-09-25) |
| D5 | 2026-09-25 | Tests stay on JUnit 5: **Jupiter + vintage 5.14.4** via `junit-bom` (last 5.x). JUnit 6.x (6.1.3 seen) deprecates the vintage engine that the JUnit 4 asserts in the core test-jar and `ArcadeDBUniversalTest` still need. Mockito is not used. | accepted (owner, 2026-09-25) |
| D6 | 2026-09-25 | **ByteBuddy 1.15.10 → 1.18.14** (current): 1.15.x can't handle Java 25 class files. ByteBuddy is a compile dependency, so Orienteer sees it transitively → Orienteer pins `byte-buddy` (+ `byte-buddy-agent`) in its P3. *Correction after measuring (2026-09-25, throwaway consumer pom with orienteer-core's declaration order):* in orienteer-core, `byte-buddy` and Mockito 2.22's are both at depth 2 and `transponder-orientdb` is declared first, so **1.18.14 wins today**; but `byte-buddy-agent` resolves to Mockito's **1.8.21** (mismatched pair), and the result depends on declaration order (Mockito 5.24 brings 1.17.7). The pin makes it deterministic. | accepted (owner, 2026-09-25: Orienteer pins in P3) |
| D7 | 2026-09-25 | **Other drivers** (arcadedb, neo4j, janusgraph, mongodb — not used by Orienteer): keep them building with fixes inside the same DB major (patch/minor bumps, the D4 GraalJS swap, Javadoc/`package-info` fixes). A driver that stays red moves to the opt-in Maven profile **`parked`** (never deleted; `./mvnw -Pparked …` still tries it). No DB-major ports in Stage A (→ X). | accepted (owner, 2026-09-25) |
| D8 | 2026-09-25 | **Parked modules are never published**, not even when built with `-Pparked` (excluded from Central publishing). | accepted (owner, 2026-09-25) |
| D9 | 2026-09-25 | POM `url` → `https://github.com/OrienteerBAP/Transponder` (was `https://orienteer.org`), like wicket-orientdb. | accepted (owner, 2026-09-25) |
| D10 | 2026-09-25 | **Publishing** via the Central Portal (`central-publishing-maven-plugin`, server id property `central.server.id`, default `central`). Agents prepare and validate locally only; every upload is run/approved by the owner. **The `org.orienteer` namespace status on the Central Portal is unconfirmed** → the snapshot deploy is a hard stop until the owner confirms the namespace is verified **with SNAPSHOTs enabled**. | accepted; namespace **open** |
| D11 | 2026-09-25 | Publishing config (`distributionManagement`, `release` profile, nexus-staging, release plugin) stays untouched in Stage A and is replaced in phase R. Only dead `<repositories>` are removed in A. Checkstyle is upgraded in A with a semantically equivalent config. | accepted |
| D12 | 2026-09-25 | `AGENTS.md` (root + per module) is the canonical agent guide; `CLAUDE.md` is only a pointer to it, so nothing is duplicated. | accepted |
| D13 | 2026-09-25 | **`transponder-neo4j` is parked** (D7). Neo4j 4.4 (4.4.38 and the last patch 4.4.48 tried) can't start on JDK 21: `UnsafeUtil.initDirectByteBuffer` writes `java.nio.Buffer.capacity` through a `VarHandle`, and that field is `final` since JDK 21 (`UnsupportedOperationException: set`); every page-cache read/write goes through it, no Neo4j toggle avoids it, and no `--add-opens` can make a final field writable. Neo4j 4.4 supports Java 11/17 only; JDK 21 needs Neo4j 5.x (DB-major port → X). Stays on 4.4.38 (the patch bump fixed nothing). | accepted (policy D7) |
| D15 | 2026-09-25 | **ArcadeDB gets the GraalJS swap too** (D7 allows it): ArcadeDB 23.12.x (23.12.2 as well) brings GraalVM 22.3.4; on JDK 25 Truffle finds no languages and `command("js", …)` fails with `Query engine 'js' was not found` (works on 21). Exclusions `org.graalvm.{sdk,truffle,js,regex}` on `arcadedb-engine` + GraalJS 25.0.4 in ArcadeDB's own roles: `polyglot` **compile** (was `graal-sdk` compile), `js` pom **runtime**. Unlike OrientDB (D4) the engine is a compile dependency here, so the replacement is too. | accepted (policy D7) |
| D16 | 2026-09-25 | **MongoDB test server 5.0 → 7.0** (test fixture only; the driver stays `mongodb-driver-sync` 5.2.1, which supports servers 4.0–8.0). flapdoodle's own resolver maps 5.0 on Ubuntu 22.04/24.04 (GitHub `ubuntu-latest`) to the `ubuntu2004` 5.0.26 build, which needs OpenSSL 1.1 — not shipped on those systems, so `mongod` can't start in CI. 7.0 resolves to the `ubuntu2204` 7.0.12 build (OpenSSL 3). MongoDB 5.0 is also EOL. Verified on macOS arm64 (7.0.12) with JDK 21 and 25; first Linux run is the first CI run. | accepted (policy D7) |
| D14 | 2026-09-25 | **`transponder-janusgraph` is parked** (D7). 6 of its 20 universal tests fail on JDK 21 exactly as in the baseline (query translation/wrapping gaps, not a JDK issue; JanusGraph 1.1.0 is the latest release). Its 2 Checkstyle violations are fixed (Javadoc-only `package-info.java`; private constructor for the static-only `JanusGraphUtils` — the module was never published) so `-Pparked` shows only the real test failures. | accepted (policy D7) |

### Open questions (decide when the phase starts; record the answer above)

- [ ] **`org.orienteer` namespace** on the Central Portal: verified? SNAPSHOTs enabled? (owner; blocks the first deploy, R)
- [ ] **Parked drivers long-term:** port Neo4j to 5.26 LTS / 2026.x, ArcadeDB to 26.x, fix JanusGraph — or drop them? (owner, X)
- [ ] **Unused GitHub secrets** `OSSRH_USERNAME`/`OSSRH_TOKEN` (used by the old `maven.yml` deploy): delete or rename after R? (owner)
- [ ] **Stale remote branch** `origin/mongodb` (fully merged): delete? (owner, X)

## Contract with Orienteer (from Orienteer's "External dependency requirements")

| Stage | Orienteer phase | Required from this library |
|---|---|---|
| A | P3 (Java 21, javax, Wicket 8) | JDK 21 build; bytecode generation lib supports Java 21/25 class files; OrientDB 3.2.56; release (or a published snapshot) |
| B | P5 (Wicket 9) | — |
| C | P6 (Wicket 10, jakarta) | — (no servlet coupling expected; verify no `javax.*`) |

Orienteer consumes `transponder-orientdb` (+ `transponder-core` transitively; the parent pom is resolved as their parent)
in `orienteer-core` `org.orienteer.core.dao` (`DAO` creates the `Transponder`; `OrienteerDriver extends ODriver` and
overrides `createType`, `onPostCreateType`, `createProperty`, `setupRelationship`, `getEntityMainClass`) and in every
module with DAO interfaces: 48 source files import `org.orienteer.transponder` — mostly `@EntityType`,
`@EntityProperty`, `@EntityPropertyIndex`, `@EntityIndex`, `@Query`, `@Lookup`, `@DefaultValue`, `@Sudo`,
`@AdviceAnnotation`, `orientdb.OrientDBProperty`, `orientdb.IODocumentWrapper`, `ODriver`, `Transponder.rewrap`,
`CommonUtils.listDeclaredMethods`. Two Orienteer files use ByteBuddy's `net.bytebuddy.asm.Advice` directly.

---

## P0 — AI initialization

- [x] Analyze the repository: modules, poms, plugins, JDK blockers, Stage B/C inventory, tests, branches — 2026-09-25
- [x] Baseline build on JDK 21 and 25 with the unchanged poms (Appendix C) and a `javap` snapshot of the public API — 2026-09-25
- [x] Root `AGENTS.md` and one per module; `CLAUDE.md` reduced to a pointer (D12) — 2026-09-25
- [x] This plan with decisions log, contract, stages, appendices — 2026-09-25

## A — Stage A: JDK 21/25 on the current majors (→ Orienteer P3)

Goal: `./mvnw clean verify` green on JDK 21 and 25 with `--release 21`; `./mvnw install` puts the parent pom,
`transponder-core` (jar + test-jar) and `transponder-orientdb` (+ the non-parked drivers) for `1.1-SNAPSHOT` into `~/.m2`.
Baseline (2026-09-25, Appendix C): JDK 21 — core, orientdb, arcadedb, mongodb green; neo4j fails to start the DB;
janusgraph 6/20 tests fail and 2 Checkstyle violations. JDK 25 — core doesn't compile (Lombok not run, JDK 23+).

- [x] A1 Maven Wrapper 3.9.16 (`mvn wrapper:wrapper -Dmaven=3.9.16 -Dtype=only-script`): commit `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` — 2026-09-25, wrapper plugin 3.3.4 (scripts identical to wicket-orientdb's)
- [x] A1 `.sdkmanrc` (`java=21.0.11-tem`) and a minimal `.editorconfig` (UTF-8, LF, tabs for `*.java`/`*.xml`; no trailing-whitespace or final-newline enforcement — 28 tracked files lack one; **no reformat**) — 2026-09-25; also 2-space YAML (workflows) and CRLF for `*.cmd`
- [x] A2 Compiler: `maven.compiler.release=21` replaces `source/target 1.8` and `<release>8</release>`; remove the `release 11` overrides in arcadedb, neo4j, janusgraph; keep `-parameters` (query parameters bind by name); compiler plugin 3.8.1 → 3.16.0 — 2026-09-25: class files major 65; JDK 21 results identical to the baseline (core/orientdb/arcadedb/mongodb green; neo4j, janusgraph red as before)
- [x] A2 Lombok 1.18.34 → 1.18.48, declared in `maven-compiler-plugin` `annotationProcessorPaths` (required from JDK 23) — 2026-09-25: core now compiles on JDK 25 and its 36 tests pass there; orientdb then fails on JDK 25 with `NoSuchMethodError: sun.misc.Unsafe.ensureClassInitialized` (GraalVM 21.3.5 → A9)
- [x] A3 Remove dead repositories: root `snapshots-repo` (oss.sonatype.org, inherited by every module) and arcadedb's `s01.oss.sonatype.org`; `distributionManagement` stays until R (D11) — 2026-09-25: `dependency:go-offline` into an empty local repo resolves everything (deps + plugins) from Maven Central only; test results unchanged
- [x] A4 Remove the `maven-bundle-plugin` build extension and `<type>bundle</type>` of `orientdb-core` — 2026-09-25: dependency tree of core/orientdb unchanged except `orientdb-core:bundle` → `:jar`; tests unchanged
- [x] A5 Pin every build plugin once in root `<pluginManagement>` (Appendix B, wicket-orientdb versions); release-profile source/javadoc/gpg pinned to current; nexus-staging stays in the profile until R — 2026-09-25: clean/resources 3.5.0, compiler 3.16.0, surefire 2.22.1 → 3.6.0, jar 3.5.1, install/deploy 3.2.0, site 3.22.0, project-info-reports 3.9.0, source 3.4.0, javadoc 3.12.0 (2.x is JDK-incompatible), gpg 3.2.8; also dependency 3.11.0, wrapper 3.3.4, versions 2.22.0 (helper goals). Checkstyle (3.1.2) and release (2.5.3) moved there unchanged — bumped in A6 and R. No plugin versions left in module poms. Tests unchanged (111 run, 0 failures)
- [x] A5 Add `maven-enforcer-plugin`: `requireMavenVersion [3.9,)`, `requireJavaVersion [21,)` — 2026-09-25 (3.6.3; checked with a throwaway `[25,)` range on JDK 21, which fails the build). Build-time only: the enforcer runs when this project is built, it isn't imposed on consumers
- [x] A6 Checkstyle plugin 3.1.2 (Checkstyle 9.0.1) → 3.6.0 (14.1.0); migrate `check_style.xml` (DTD 1.3, same rules); still `failOnViolation` at `verify` — 2026-09-25: only the DTD changed (the config already used the split `Missing*` checks and a Checker-level `LineLength`). The new Javadoc parser rejected `{@link Object[]}` in `CommonUtils.toMap` → `{@code Object[]}` (Javadoc only; also a doclint error). Then 0 violations in all 7 modules (incl. parked). Each rule re-checked with throwaway probe files: missing type/method Javadoc, wrong/missing `@param`/`@return`, constant name, interface without `I`, line > 200, anonymous class > 60 lines, utility class with public constructor, missing `package-info.java` all reported
- [x] A7 JUnit 5.11.3 → 5.14.4 via `junit-bom` (Jupiter + vintage, D5); Hamcrest 3.0 unchanged — 2026-09-25: property `junit.version`; test class path now JUnit Platform 1.14.4, `junit:junit` 4.13.2 (via vintage). 111 run / 0 failures; the 2 JUnit 4 tests of `ArcadeDBUniversalTest` still run through vintage
- [x] A8 ByteBuddy 1.15.10 → 1.18.14 (D6) — 2026-09-25: property `bytebuddy.version`. JDK 21: 111/0. JDK 25: core, arcadedb, mongodb green; orientdb still blocked by GraalVM 21.3.5 (A9). Precision on the risk (probe on JDK 25): 1.15.10 still *generates* classes on JDK 25 and core's tests passed there after A2, because Transponder's own classes are `--release 21`; it throws `Java 25 (69) is not supported` as soon as it must *parse* a Java 25 class file (JDK classes through `TypePool`, advice/entity classes compiled for 25). 1.18.14 reads up to Java 27
- [x] A8 `CommonUtils.listDeclaredMethods` visitor API `ASM7` vs Java 17+ class features: a `sealed` interface throws `UnsupportedOperationException: PermittedSubclasses requires ASM9` (reproduced) → ASM9 + test — 2026-09-25: a `sealed` `@EntityType` interface (legal once Orienteer is on Java 21) would have crashed `Transponder.define`. Visitor API level `Opcodes.ASM9` (same visitor, the default callbacks for newer attributes are no-ops; signature unchanged). New `UtilsTest.testListDeclaredMethodsOfSealedInterface` fails before, passes after (core: 37 tests)
- [x] A9 OrientDB 3.2.36 → 3.2.56 (D3, `provided`); record the transitive diff in the handoff — 2026-09-25: property `orientdb.version`. JDK 21: 112/0 (orientdb 33/0). Diff, all `provided` (nothing reaches consumers): `org.lz4:lz4-java` 1.8.0 → `at.yawk.lz4:lz4-java` 1.11.0, `commons-lang` 2.6 removed, `jackson-core` 2.15.2 → 2.22.0; GraalVM still 21.3.5 (next item). `ODriver.query/querySingle/command` already collect inside the try-with-resources, so the closed-`OResultSet` behavior change of 3.2.56 doesn't affect them
- [x] A9 GraalJS swap in test scope (D4); `dependency:tree -Dincludes='org.graalvm*'` shows only 25.0.4, test scope — 2026-09-25: exclusions on the `provided` `orientdb-core`, `polyglot`/`js-scriptengine`/`js` (pom) 25.0.4 `test`, property `graalvm.version`. The tree now has **no** Graal artifact outside `test` scope (before: 21.3.5 `provided`). A throwaway probe (`db.execute("javascript", "20 + 22")` on the embedded DB) returned 42 on JDK 21.0.11 and 25.0.4. **`./mvnw clean verify` is now green on JDK 25 too:** 112 run / 0 failures on both JDKs
- [x] A10 Drivers (D7): neo4j and janusgraph → profile `parked` (D13, D14); arcadedb and mongodb stay in the default build (green on JDK 21 and 25 without changes) — 2026-09-25. Done right after A4 so every later commit has a green default build; `./mvnw -Pparked verify` builds both parked drivers with 0 Checkstyle violations, their tests fail as documented. README updated in A12
- [x] A10 ArcadeDB on JDK 25: its GraalVM 22.3.4 logs `GraalVM Polyglot Engine: no languages found` (JS unusable; tests don't run JS) → GraalJS 25.0.4 swap for `arcadedb-engine` (compile scope there: ArcadeDB's own scripting), verified with a JS query on 21 and 25 — 2026-09-25 (D15): throwaway probe `command("js", "20 + 22")` → before: 42 on JDK 21, `Query engine 'js' was not found` on 25; after: 42 on both. 23.12.2 (the only newer 23.12 patch) still has Graal 22.3.4, so no version bump. Default reactor 112/0 on 21 and 25, no "no languages found" any more
- [x] A11 Surefire `argLine`: keep/add `--add-opens` only where a test run proves it's needed; list them in the handoff — 2026-09-25: default reactor needs **none** (112/0 on 21 and 25 without any flag). Parked drivers, tested with the pom `argLine` removed (a `-DargLine=` override doesn't beat pom config): neo4j **needs** its 3 `--add-opens` (otherwise `IllegalAccessException: java.base does not open java.nio` before the known D13 failure) → kept, with a comment; janusgraph gives the same 14/20 with and without them on 21 and 25 → removed
- [x] A11 Library hygiene: Lombok `provided`; no logging binding or logging config in any main jar (`jar tf`); no servlet API; no needless hard pins — 2026-09-25: main jars = classes + Maven metadata only; test-jar = classes + `META-INF/transponder/polyglot.properties`. Compile/runtime scope of core/orientdb: Guava 33.4.8 (+ its annotations), ByteBuddy 1.18.14, Objenesis 3.2 — no logging, servlet, `javax.*`/`jakarta.*` or Lombok; OrientDB stays `provided`, GraalJS `test`. The only version pins consumers see are ordinary dependency versions (overridable via their `dependencyManagement`); the parent pom has no `<repositories>` any more
- [x] A12 `./mvnw clean verify` green on JDK 21 **and** 25; record test counts in Appendix C — 2026-09-25: both 112 run / 0 failures / 0 errors / 0 skipped; Checkstyle 0 violations in all 5 modules
- [x] A12 `./mvnw install`; confirm `~/.m2/repository/org/orienteer/transponder/`: parent pom, core jar + `-tests.jar`, orientdb jar; class files major version 65 — 2026-09-25: parent pom, core jar (59 classes) + `-tests.jar` (52), orientdb jar (6), arcadedb (5), mongodb (4); every class file major 65; manifests `Build-Jdk-Spec: 21`
- [x] A12 Public API diff of core and orientdb vs the baseline `javap` snapshot (expected: none) — 2026-09-25: **no change** in any public/protected member of core, orientdb, arcadedb, mongodb. The only `javap` difference is the synthetic `package-info` classes that javac emits for `--release 9+` (Javadoc-only `package-info.java` files existed before; no annotations in them)
- [x] A12 Update `AGENTS.md` files and README (Java 21), write "Handoff to Orienteer" below — 2026-09-25: README "Java Version Requirements" rewritten (Java 21+, 21/25 matrix, parked drivers); the Travis badge and OSSRH snippet are phase R
- [x] Exit check: `./mvnw clean verify` green on JDK 21 and 25; `./mvnw install` done; test results match Appendix C (differences explained) — 2026-09-25: 112 = baseline's 111 green tests + 1 new (`UtilsTest`, A8); parked neo4j/janusgraph fail exactly as in the baseline (Appendix C). **Stage A done**

## B — Stage B: Orienteer P5 (Wicket 9, still javax)

Goal: confirm that Orienteer P5 can keep using the Stage A release. Size: **XS (< 1 hour)**.
Transponder has no Wicket, servlet, Guice or `javax.*` coupling (Appendix D), so nothing needs to change.

- [ ] Re-run the Appendix D inventory (`rg 'javax\.|org\.apache\.wicket|com\.google\.inject'` over `src/`) and `dependency:tree` of core and orientdb (no `javax.*` in compile/runtime scope)
- [ ] ByteBuddy alignment note for Orienteer: wicket-ioc 9.24 depends on ByteBuddy 1.14.12, which can't read Java 25 class files and would win by nearest in Orienteer — Orienteer's `byte-buddy` pin (D6) must stay ≥ the Transponder version
- [ ] If a change is needed after all: version `1.2-SNAPSHOT` (D2), never `1.1-SNAPSHOT`
- [ ] Exit check: inventory empty; Orienteer P5 compiles against the Stage A release

## C — Stage C: Orienteer P6 (Wicket 10, jakarta)

Goal: confirm no `javax.servlet`/`javax.inject`/`javax.mail` in this library or its dependencies. Size: **XS (< 1 hour)**.

- [ ] Re-run the Appendix D inventory; note: `orientdb-core` 3.2.x itself declares `javax.activation-api` (compile) — it's `provided` here, so it's Orienteer's dependency, not ours
- [ ] Optional: enforcer `bannedDependencies` for `javax.servlet:*`, `javax.inject:javax.inject`, `javax.mail:*` in compile/runtime scope
- [ ] ByteBuddy: wicket-ioc 10.11 uses 1.18.4 — compatible with the Stage A version
- [ ] If an API-breaking change is ever needed: new major `2.0-SNAPSHOT` (D2)
- [ ] Exit check: inventory empty; Orienteer P6 compiles against the release

## R — Release & CI

- [x] Prerequisite for CI: MongoDB test server 5.0 → 7.0 (D16) — 2026-09-25: `MongoDBUniversalTest` 20/0 on JDK 21 and 25 (macOS arm64, `mongodb-macos-arm64-7.0.12`)
- [ ] GitHub Actions `ci.yml`: push + PR, matrix JDK 21/25 (`actions/setup-java`, `distribution: temurin`, `cache: maven`), `./mvnw -B verify`, surefire reports, `concurrency`; no publishing in CI (D10). Remove the old `maven.yml` (deploys to dead OSSRH with JDK 11 on every push)
- [ ] Remove `.travis.yml`; remove the Travis badge from `README.md`; README snapshot-repository snippet → Central Portal snapshots
- [ ] Replace OSSRH `distributionManagement` and `nexus-staging-maven-plugin` with `org.sonatype.central:central-publishing-maven-plugin` (0.11.0 seen), server id `${central.server.id}` (default `central`); exclude parked modules (D8)
- [ ] Release profile: sources + test-sources + javadoc jars (fix doclint errors), GPG signing (`maven-gpg-plugin` 3.x), required POM metadata (name, description, url (D9), licenses, developers, scm) valid in every published module
- [ ] maven-release-plugin 2.5.3 → 3.x; tag format `v@{project.version}`; `pushChanges=false` (owner pushes)
- [ ] `.github/dependabot.yml` (maven + github-actions, weekly, grouped); hold DB majors and JUnit 6 until decided
- [ ] JaCoCo report only (no thresholds)
- [ ] Local validation without any upload: `-Prelease -Dgpg.skip`; list the bundle contents
- [ ] Owner: confirm the `org.orienteer` namespace (verified, SNAPSHOTs enabled), then run the first `1.1-SNAPSHOT` deploy (D10)
- [ ] Owner: release `1.1` once Orienteer P3 is green against `1.1-SNAPSHOT`; then `master` → `1.2-SNAPSHOT` (D2)
- [ ] Exit check: `1.1-SNAPSHOT` (later `1.1`) resolves from the Central Portal in a clean `~/.m2`; Orienteer's "Stage A delivered" item can be ticked

## X — Cleanup, docs, parked drivers, known bugs

### Docs & cleanup
- [ ] README: fix typos in examples (`Tranponder.save`, `IFodler`, `Transponder.save()` without argument, "Suppport")
- [ ] `.gitignore`: `.DS_Store`, `.vscode/`
- [ ] Test-jar and `ArcadeDBUniversalTest` still use JUnit 4 asserts/annotations → Jupiter; then drop `junit-vintage-engine` (and consider JUnit 6)
- [ ] Guava 33.4.8 → current, Objenesis 3.2 → 3.6 (no functional need in A)
- [ ] Remove `System.out.println` from `DAOTest`
- [ ] Stale remote branch `origin/mongodb` (fully merged) — owner decision
- [ ] Keep all `AGENTS.md` files current at the end of every stage

### Parked / other drivers (DB-major decisions, D7)
- [ ] Neo4j (parked, D13): port to Neo4j 5.26 LTS or 2026.x (Java 21+): new embedded API packages, Cypher 5, `neo4j` artifact size; then un-park — or drop the driver (owner)
- [ ] JanusGraph (parked, D14): fix the 6 failing universal tests (`JanusGraphDriver` query translation, auto-(un)wrapping, lookups), then un-park — or drop (owner)
- [ ] ArcadeDB 23.12 → 26.x (DB major, Java 21 baseline) — optional; 23.12 works on 21/25

### Known bugs / tech debt
- [ ] `JanusGraphDriver`: only basic SQL → Gremlin translation; 6 of 20 universal tests fail (pre-existing, Appendix C)

---

## Appendix A — Dependency versions (current → target)

"Latest" = latest stable on Maven Central on 2026-09-25.

| Dependency | Current | Latest seen | Target | Stage |
|---|---|---|---|---|
| JDK (`release`) | 8 (arcadedb/neo4j/janusgraph: 11) | 25 LTS (27 non-LTS) | 21, verified on 21 + 25 | A |
| ByteBuddy (compile, all modules) | 1.15.10 | 1.18.14 | 1.18.14 (D6) | A |
| Objenesis (compile) | 3.2 | 3.6 | keep (X) | — |
| Guava (core, compile) | 33.4.8-jre | 33.7.1-jre | keep (X) | — |
| Lombok (`provided`) | 1.18.34 | 1.18.48 | 1.18.48 | A |
| JUnit Jupiter + vintage (test) | 5.11.3 | 6.1.3 (5.x: 5.14.4) | 5.14.4 (D5) | A |
| Hamcrest (test) | 3.0 | 3.0 | 3.0 | — |
| OrientDB `orientdb-core` (`provided`) | 3.2.36 | 3.2.56 | 3.2.56 (D3) | A |
| GraalVM/GraalJS (via OrientDB) | 21.3.5 (crashes on JDK 22+) | 25.4.4.1.1 (25.0.x: 25.0.4) | 25.0.4, test scope (D4) | A |
| ArcadeDB `arcadedb-engine` | 23.12.1 (brings Graal 22.3.4) | 26.9.1 (23.12.x: 23.12.2) | 23.12.x (D7) | A |
| Neo4j `neo4j` (embedded) | 4.4.38 | 2026.09.0 (5.26.31 LTS, 4.4.48) | 4.4.48 or park (D7) | A |
| JanusGraph `janusgraph-core`/`-inmemory` | 1.1.0 | 1.1.0 (1.2.0 only as dated snapshots) | 1.1.0 | — |
| TinkerPop `gremlin-core` | 3.7.3 | 3.8.2 (3.7.x: 3.7.7) | 3.7.x (D7) | A |
| MongoDB `mongodb-driver-sync` | 5.2.1 | 5.12.0 | 5.x (D7) | A |
| flapdoodle `de.flapdoodle.embed.mongo` (test) | 4.17.0 | 5.0.0 (4.x: 4.33.0) | 4.x (D7) | A |
| Maven | 3.9.16 (Homebrew) | 3.9.16 (4.0 RC) | wrapper 3.9.16 | A |

## Appendix B — Maven plugins (current → latest seen 2026-09-25)

| Plugin | Current | Latest seen | Action (stage) |
|---|---|---|---|
| maven-compiler-plugin | 3.8.1 (`release` 8) | 3.16.0 | bump, `release` 21, Lombok APT path (A) |
| maven-surefire-plugin | 2.22.1 | 3.6.0 | bump (A) |
| maven-clean / resources / jar / install / deploy | 3.1.0 / 3.0.2 / 3.0.2 / 2.5.2 / 2.8.2 | 3.5.0 / 3.5.0 / 3.5.1 / 3.2.0 / 3.2.0 | bump (A) |
| maven-site / project-info-reports | 3.7.1 / 3.0.0 | 3.22.0 / 3.9.0 | bump (A) |
| maven-checkstyle-plugin (+ Checkstyle) | 3.1.2 (9.0.1) | 3.6.0 (14.1.0) | bump + config migration (A) |
| maven-enforcer-plugin | — | 3.6.3 | add (A) |
| maven-dependency / versions / wrapper (helper goals) | unpinned | 3.11.0 / 2.22.0 / 3.3.4 | pin (A) |
| maven-source / javadoc / gpg (release profile) | 3.0.0 / 2.10.3 / 1.6 | 3.4.0 / 3.12.0 / 3.2.8 | pin (A), wire up (R) |
| maven-release-plugin | 2.5.3 | 3.3.1 | bump (R) |
| nexus-staging-maven-plugin | 1.6.7 | OSSRH gone | → central-publishing-maven-plugin 0.11.0 (R) |
| org.apache.felix:maven-bundle-plugin (extension) | 3.0.1 | 6.x | remove with `type=bundle` (A) |
| jacoco-maven-plugin | — | 0.8.15 | add, report only (R) |

## Appendix C — Test inventory

Static count (2026-09-25): 151 JUnit tests, none `@Disabled`/`@Ignore`d (`ByteBuddyTest` line 158 is a custom `@Test`
annotation, not JUnit). The 20 tests of `AbstractUniversalTest` (core test-jar) run once per driver.

| Module | Test classes | Tests | Baseline JDK 21.0.11 (surefire 2.22.1): run / fail / error | Baseline JDK 25.0.4 |
|---|---|---|---|---|
| core | `CoreUniversalTest` 20, `CoreSpecificTest` 9, `ByteBuddyTest` 5, `UtilsTest` 2 (3 from A8) | 36 | 36 / 0 / 0 | compile error (Lombok not run) |
| orientdb | `DAOTest` 13, `OrientDBUniversalTest` 20 | 33 | 33 / 0 / 0 | not reached |
| arcadedb | `ArcadeDBUniversalTest` 20 + 2 (JUnit 4, vintage) | 22 | 22 / 0 / 0 | not reached |
| neo4j | `Neo4JUniversalTest` 20 | 20 | 1 / 0 / 1 — DB start fails: `UnsupportedOperationException: set` ("Failed to wrap pointer in ByteBuffer") | not reached |
| janusgraph | `JanusGraphUniversalTest` 20 | 20 | 20 / 6 / 0 (`testAutoWrapping`, `testAutoUnwrapping`, `testDAOQuery`, `testCommand`, `testLookupInDAO`, `testLookupInEntity`) + 2 Checkstyle violations (no `package-info.java`, `HideUtilityClassConstructor`) | not reached |
| mongodb | `MongoDBUniversalTest` 20 (flapdoodle downloads MongoDB 5.0) | 20 | 20 / 0 / 0 | not reached |
| **total** | | **151** | **132 pass, 6 fail, 1 error** (neo4j's 19 others never ran) | build fails in core |

Stage A exit (A12, surefire 3.6.0, JUnit 5.14.4, ByteBuddy 1.18.14, OrientDB 3.2.56, GraalJS 25.0.4):

| Module | JDK 21.0.11: run / fail / error / skipped | JDK 25.0.4 | Notes |
|---|---|---|---|
| core | 37 / 0 / 0 / 0 | 37 / 0 / 0 / 0 | +1 `UtilsTest.testListDeclaredMethodsOfSealedInterface` (A8) |
| orientdb | 33 / 0 / 0 / 0 | 33 / 0 / 0 / 0 | JDK 25 needed the GraalJS swap (A9) |
| arcadedb | 22 / 0 / 0 / 0 | 22 / 0 / 0 / 0 | 2 via vintage |
| mongodb | 20 / 0 / 0 / 0 | 20 / 0 / 0 / 0 | |
| **default reactor** | **112 / 0 / 0 / 0** | **112 / 0 / 0 / 0** | nothing skipped or disabled |
| neo4j (parked, `-Pparked`) | 1 / 0 / 1 | 1 / 0 / 1 | D13: DB can't start on JDK 21+ |
| janusgraph (parked, `-Pparked`) | 20 / 6 / 0 | 20 / 6 / 0 | D14: same 6 as the baseline |

## Appendix D — Stage B/C inventory (2026-09-25, `rg` over `transponder-*/src`)

| Item | Where | Stage |
|---|---|---|
| `javax.servlet`, `javax.inject`, `javax.mail`, `javax.xml.bind`, `javax.annotation`, `javax.script` | none | — |
| Apache Wicket (any API, incl. `WicketTesterScope`, `util.time`, page store, `ModalWindow`) | none | — |
| Guice, cglib, Mockito, javassist, `sun.*`, JDK-internal reflection | none (only `setAccessible` on a generated class in a test) | — |
| `javax.*` artifacts in compile/runtime scope of core/orientdb | none (`orientdb-core`'s `javax.activation-api` is `provided` here) | — |
| ByteBuddy version alignment with wicket-ioc (9.24 → 1.14.12, 10.11 → 1.18.4) | Orienteer's `dependencyManagement` | B/C (Orienteer) |

## Appendix E — Useful commands

```bash
./mvnw versions:display-dependency-updates versions:display-plugin-updates   # what's outdated
./mvnw -pl transponder-orientdb dependency:tree -Dincludes='org.graalvm*'    # who pulls a dependency
./mvnw -pl transponder-core test -Dtest=CoreSpecificTest                     # one test class
./mvnw -Pparked -pl transponder-neo4j -am verify                             # try a parked driver (once A10 parks it)
javap -v -cp transponder-core/target/classes org.orienteer.transponder.Transponder | grep major
```

## Handoff to Orienteer

Stage A, 2026-09-25. **`1.1-SNAPSHOT` is installed locally only** (`~/.m2` of the dev machine); it is not published yet —
see §8 (phase R) for what the owner has to do first. `1.1` is released after Orienteer P3 is green against the snapshot (D2).

### 1. Installed artifacts (`~/.m2/repository/org/orienteer/transponder/`)

| Coordinates | File |
|---|---|
| `org.orienteer.transponder:transponder-parent:1.1-SNAPSHOT:pom` | `transponder-parent/1.1-SNAPSHOT/transponder-parent-1.1-SNAPSHOT.pom` |
| `org.orienteer.transponder:transponder-core:1.1-SNAPSHOT:jar` | `transponder-core/1.1-SNAPSHOT/transponder-core-1.1-SNAPSHOT.jar` (59 classes) |
| `org.orienteer.transponder:transponder-core:1.1-SNAPSHOT:test-jar` (classifier `tests`) | `transponder-core/1.1-SNAPSHOT/transponder-core-1.1-SNAPSHOT-tests.jar` (52 classes; driver test suite, not needed by Orienteer) |
| `org.orienteer.transponder:transponder-orientdb:1.1-SNAPSHOT:jar` | `transponder-orientdb/1.1-SNAPSHOT/transponder-orientdb-1.1-SNAPSHOT.jar` (6 classes) |
| (not used by Orienteer) `transponder-arcadedb`, `transponder-mongodb` `1.1-SNAPSHOT:jar` | `transponder-{arcadedb,mongodb}/1.1-SNAPSHOT/` |
| parked, **not installed/published**: `transponder-neo4j`, `transponder-janusgraph` | — (D8, D13, D14) |

- **Bytecode:** `--release 21`, so every class file is major version **65**; manifests say `Build-Jdk-Spec: 21`. JDK 17 can't load them.
- **Built and tested with:** Temurin 21.0.11 and Temurin 25.0.4, Maven 3.9.16 through `./mvnw`.

### 2. Versions compiled against

| Library | Version | Scope in the library pom |
|---|---|---|
| ByteBuddy (`net.bytebuddy:byte-buddy`) | **1.18.14** (was 1.15.10) | compile (core, inherited by every module) |
| Objenesis | 3.2 | compile |
| Guava | 33.4.8-jre | compile (core) |
| OrientDB `orientdb-core` | **3.2.56** (was 3.2.36) | **provided** — Orienteer's version wins |
| GraalJS (`polyglot`, `js-scriptengine`, `js` pom) | 25.0.4, replacing OrientDB's GraalVM 21.3.5 | **test** only (D4) |
| Lombok | 1.18.48 (annotation processor path) | provided |
| JUnit Jupiter + vintage / Hamcrest | 5.14.4 / 3.0 | test |
| Servlet API, Wicket, Guice, `javax.*`, logging | not used | — |

### 3. JVM flags Orienteer must mirror

- **Required: none.** All 112 tests pass on JDK 21 and 25 without `--add-opens`/`--add-exports` (ByteBuddy defines the
  generated classes with `ClassLoadingStrategy.Default.WRAPPER`, a plain child class loader — no `Unsafe`, no `Lookup` injection).
- JDK 25 prints `sun.misc.Unsafe::objectFieldOffset` deprecation warnings from Lombok (build time only) and from OrientDB's
  `concurrentlinkedhashmap-lru` 1.4.2 — informational; `--sun-misc-unsafe-memory-access=allow` silences them (JDK 23+ only;
  JDK 21 refuses the flag). Truffle's `System::load` warning: `--enable-native-access=ALL-UNNAMED`. Same list as wicket-orientdb's handoff.

### 4. Actions required in Orienteer (P1/P3)

1. **Pin ByteBuddy** (D6, accepted by the owner for P3). Transponder is built and tested with `byte-buddy` 1.18.14.
   Measured with a throwaway pom that copies orienteer-core's declaration order (transponder-orientdb before
   `mockito-core` 2.22.0 test): `byte-buddy` resolves to **1.18.14** (equal depth 2, first declaration wins), but
   `byte-buddy-agent` resolves to Mockito's **1.8.21** — a mismatched pair in orienteer-core's tests, and a result that
   flips if the dependencies are reordered. Mockito 5.24 (Orienteer P3) brings 1.17.7 (reads Java 25, but older than
   what Transponder is tested with); `wicket-ioc` 9.24 (P5) brings 1.14.12 (can't read Java 25 class files), at depth 3
   in orienteer-core, so it loses today. Pin both in the root `dependencyManagement` so no order or upgrade can change it:
   ```xml
   <dependency><groupId>net.bytebuddy</groupId><artifactId>byte-buddy</artifactId><version>1.18.14</version></dependency>
   <dependency><groupId>net.bytebuddy</groupId><artifactId>byte-buddy-agent</artifactId><version>1.18.14</version></dependency>
   ```
   Check with `./mvnw -pl orienteer-core dependency:tree -Dincludes=net.bytebuddy -Dscope=test` (all entries 1.18.14).
   Two Orienteer files use `net.bytebuddy.asm.Advice` directly (`TestDAOMethodHandler`, logger-server `IOLoggerEventModel`): unaffected.
2. **GraalVM exclusions on Orienteer's own `orientdb-core`**: already planned (Orienteer D8, P3). Transponder no longer
   brings any Graal artifact to consumers (it was `provided` 21.3.5 before, now test-only 25.0.4), so nothing to add for it.
3. **Sealed `@EntityType` interfaces** now work (A8 fix in `CommonUtils.listDeclaredMethods`); before, `Transponder.define`
   threw `UnsupportedOperationException: PermittedSubclasses requires ASM9` for any sealed interface.
4. **Repository:** until `1.1-SNAPSHOT` is deployed (§8), Orienteer needs a local `./mvnw install` of this repo. After the
   deploy, the Central Portal snapshots repository that Orienteer's root pom already declares (P1) resolves it.

### 5. Public API changes

- **None** in `transponder-core` and `transponder-orientdb` (verified by diffing `javap -protected` of every class
  against the pre-Stage-A build). `OrienteerDriver`'s overridden methods and `ODriver`'s protected `getSession`/`getSchema` are unchanged.
- Main-source changes (implementation/Javadoc only): `CommonUtils.listDeclaredMethods` visitor API level `ASM7` → `ASM9`
  (A8); `{@link Object[]}` → `{@code Object[]}` in `CommonUtils.toMap` Javadoc (A6); janusgraph (parked, never published):
  `package-info.java`, private constructor for `JanusGraphUtils`.

### 6. Transitive dependency changes Orienteer can see

For a consumer declaring only `transponder-orientdb` (throwaway consumer pom resolved against `~/.m2`):

| Change | Artifacts |
|---|---|
| upgraded | `net.bytebuddy:byte-buddy` 1.15.10 → **1.18.14** |
| unchanged | `transponder-core`, Guava 33.4.8-jre (+ failureaccess 1.0.3, listenablefuture, jspecify 1.0.0, error_prone_annotations 2.36.0, j2objc-annotations 3.0.0), Objenesis 3.2 |
| not transitive (as before) | `orientdb-core` (`provided`), Lombok (`provided`), GraalJS (`test`), JUnit/Hamcrest (`test`) |

The parent pom no longer declares `<repositories>` (the dead oss.sonatype.org entry is gone), so consumers inherit none.

### 7. Test results

| JDK | Default reactor (core, orientdb, arcadedb, mongodb) | Parked (`-Pparked`) |
|---|---|---|
| Temurin 21.0.11 | 112 run, 0 failures, 0 errors, 0 skipped | neo4j 1 error (DB can't start), janusgraph 6/20 fail |
| Temurin 25.0.4 | 112 run, 0 failures, 0 errors, 0 skipped | same |

Nothing is `@Disabled`/`@Ignore`d. Baseline (Appendix C): 111 of these passed on JDK 21, JDK 25 didn't compile;
+1 test added (A8). The parked drivers fail exactly as before Stage A (D13, D14). Checkstyle 14.1.0: 0 violations.

### 8. Work for Stages B and C

- **Stage B** (Orienteer P5, Wicket 9) — **nothing to change**, estimate **XS (< 1 h)** to re-verify: no Wicket, servlet,
  Guice or `javax.*` in the code or in compile/runtime dependencies (Appendix D). Only Orienteer-side: keep the ByteBuddy
  pin ≥ 1.18.14 because `wicket-ioc` 9.24 brings 1.14.12.
- **Stage C** (Orienteer P6, jakarta) — **nothing to change**, estimate **XS (< 1 h)**: same inventory; Wicket 10.11 uses
  ByteBuddy 1.18.4 (compatible). Optional: enforcer `bannedDependencies` for `javax.servlet`/`javax.inject`/`javax.mail`.
- Any future change goes to `1.2-SNAPSHOT` (after the `1.1` release), never to `1.1-SNAPSHOT` (D2).
