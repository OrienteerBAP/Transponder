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
| A | Stage A: JDK 21/25 build with `--release 21` on the **same** majors (OrientDB 3.2.x, ByteBuddy 1.x, JUnit 5), `./mvnw install` as `1.1-SNAPSHOT` | P0 | P3 | todo |
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
| D6 | 2026-09-25 | **ByteBuddy 1.15.10 → 1.18.14** (current): 1.15.x can't handle Java 25 class files. ByteBuddy is a compile dependency, so Orienteer sees it transitively — and its nearest-wins resolution picks Mockito 2.22's ByteBuddy 1.8.21 (and later Wicket 9's 1.14.12) instead → Orienteer pins `byte-buddy` (+ `byte-buddy-agent`) in its P3. | accepted (owner, 2026-09-25: Orienteer pins in P3) |
| D7 | 2026-09-25 | **Other drivers** (arcadedb, neo4j, janusgraph, mongodb — not used by Orienteer): keep them building with fixes inside the same DB major (patch/minor bumps, the D4 GraalJS swap, Javadoc/`package-info` fixes). A driver that stays red moves to the opt-in Maven profile **`parked`** (never deleted; `./mvnw -Pparked …` still tries it). No DB-major ports in Stage A (→ X). | accepted (owner, 2026-09-25) |
| D8 | 2026-09-25 | **Parked modules are never published**, not even when built with `-Pparked` (excluded from Central publishing). | accepted (owner, 2026-09-25) |
| D9 | 2026-09-25 | POM `url` → `https://github.com/OrienteerBAP/Transponder` (was `https://orienteer.org`), like wicket-orientdb. | accepted (owner, 2026-09-25) |
| D10 | 2026-09-25 | **Publishing** via the Central Portal (`central-publishing-maven-plugin`, server id property `central.server.id`, default `central`). Agents prepare and validate locally only; every upload is run/approved by the owner. **The `org.orienteer` namespace status on the Central Portal is unconfirmed** → the snapshot deploy is a hard stop until the owner confirms the namespace is verified **with SNAPSHOTs enabled**. | accepted; namespace **open** |
| D11 | 2026-09-25 | Publishing config (`distributionManagement`, `release` profile, nexus-staging, release plugin) stays untouched in Stage A and is replaced in phase R. Only dead `<repositories>` are removed in A. Checkstyle is upgraded in A with a semantically equivalent config. | accepted |
| D12 | 2026-09-25 | `AGENTS.md` (root + per module) is the canonical agent guide; `CLAUDE.md` is only a pointer to it, so nothing is duplicated. | accepted |

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
- [ ] A5 Pin every build plugin once in root `<pluginManagement>` (Appendix B, wicket-orientdb versions); release-profile source/javadoc/gpg pinned to current; nexus-staging stays in the profile until R
- [ ] A5 Add `maven-enforcer-plugin`: `requireMavenVersion [3.9,)`, `requireJavaVersion [21,)`
- [ ] A6 Checkstyle plugin 3.1.2 (Checkstyle 9.0.1) → 3.6.0 (14.1.0); migrate `check_style.xml` (DTD 1.3, same rules); still `failOnViolation` at `verify`
- [ ] A7 JUnit 5.11.3 → 5.14.4 via `junit-bom` (Jupiter + vintage, D5); Hamcrest 3.0 unchanged
- [ ] A8 ByteBuddy 1.15.10 → 1.18.14 (D6); check `CommonUtils.listDeclaredMethods` (ByteBuddy's repackaged ASM, visitor API level `ASM7`) against Java 17+ class features (sealed/records)
- [ ] A9 OrientDB 3.2.36 → 3.2.56 (D3, `provided`); record the transitive diff in the handoff
- [ ] A9 GraalJS swap in test scope (D4); `dependency:tree -Dincludes='org.graalvm*'` shows only 25.0.4, test scope
- [ ] A10 Drivers (D7), one commit each: arcadedb, neo4j, janusgraph, mongodb — fix within the same DB major or park; record each decision in the decisions log and README
- [ ] A11 Surefire `argLine`: keep/add `--add-opens` only where a test run proves it's needed; list them in the handoff
- [ ] A11 Library hygiene: Lombok `provided`; no logging binding or logging config in any main jar (`jar tf`); no servlet API; no needless hard pins
- [ ] A12 `./mvnw clean verify` green on JDK 21 **and** 25; record test counts in Appendix C
- [ ] A12 `./mvnw install`; confirm `~/.m2/repository/org/orienteer/transponder/`: parent pom, core jar + `-tests.jar`, orientdb jar; class files major version 65
- [ ] A12 Public API diff of core and orientdb vs the baseline `javap` snapshot (expected: none)
- [ ] A12 Update `AGENTS.md` files and README (Java 21), write "Handoff to Orienteer" below
- [ ] Exit check: `./mvnw clean verify` green on JDK 21 and 25; `./mvnw install` done; test results match Appendix C (differences explained)

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
- [ ] Filled in by A10 (one item per driver that ends up parked or needs a major port)

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
| core | `CoreUniversalTest` 20, `CoreSpecificTest` 9, `ByteBuddyTest` 5, `UtilsTest` 2 | 36 | 36 / 0 / 0 | compile error (Lombok not run) |
| orientdb | `DAOTest` 13, `OrientDBUniversalTest` 20 | 33 | 33 / 0 / 0 | not reached |
| arcadedb | `ArcadeDBUniversalTest` 20 + 2 (JUnit 4, vintage) | 22 | 22 / 0 / 0 | not reached |
| neo4j | `Neo4JUniversalTest` 20 | 20 | 1 / 0 / 1 — DB start fails: `UnsupportedOperationException: set` ("Failed to wrap pointer in ByteBuffer") | not reached |
| janusgraph | `JanusGraphUniversalTest` 20 | 20 | 20 / 6 / 0 (`testAutoWrapping`, `testAutoUnwrapping`, `testDAOQuery`, `testCommand`, `testLookupInDAO`, `testLookupInEntity`) + 2 Checkstyle violations (no `package-info.java`, `HideUtilityClassConstructor`) | not reached |
| mongodb | `MongoDBUniversalTest` 20 (flapdoodle downloads MongoDB 5.0) | 20 | 20 / 0 / 0 | not reached |
| **total** | | **151** | **132 pass, 6 fail, 1 error** (neo4j's 19 others never ran) | build fails in core |

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
