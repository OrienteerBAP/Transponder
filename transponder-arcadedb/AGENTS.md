# transponder-arcadedb

ArcadeDB driver (`org.orienteer.transponder:transponder-arcadedb`). **Not used by Orienteer**; kept building under the
driver policy (plan D7: fixes within the same DB major, otherwise the `parked` profile).
Root rules: [`../AGENTS.md`](../AGENTS.md); plan: [`../REFRESH_PLAN.md`](../REFRESH_PLAN.md).

## Key classes (`org.orienteer.transponder.arcadedb`)

- `ArcadeDBDriver implements IDriver` — bound to one ArcadeDB `Database`; schema via `Schema`/`DocumentType`,
  entities are ByteBuddy subclasses of `DocumentWrapper` (holds a `Document`/`MutableDocument`).
- `@ArcadeDBProperty` — ArcadeDB-specific property settings; `ArcadeDBUtils` (`@UtilityClass`) — index-type mapping.

## Dependencies

- `arcadedb-engine` 23.12.x (compile). It pulls GraalVM 22.3.4 (`graal-sdk`, `js` runtime) for its script engine.

## Tests

- `ArcadeDBUniversalTest`: the 20 universal tests + 2 ArcadeDB-specific ones written with **JUnit 4** annotations
  (`@BeforeClass`, `org.junit.Test`) — they run through the vintage engine. DB files under `target/db`.
