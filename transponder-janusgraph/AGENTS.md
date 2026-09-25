# transponder-janusgraph

JanusGraph driver (`org.orienteer.transponder:transponder-janusgraph`), JanusGraph 1.1 on Apache TinkerPop 3.7.
**Not used by Orienteer**, never published so far; kept under the driver policy (plan D7: fixes within the same DB
major, otherwise the `parked` profile). Root rules: [`../AGENTS.md`](../AGENTS.md); plan: [`../REFRESH_PLAN.md`](../REFRESH_PLAN.md).

**Parked** (profile `parked`, plan D14): not built by default, never published. Try it with `./mvnw -Pparked -pl transponder-core,transponder-janusgraph verify`.

## Key classes (`org.orienteer.transponder.janusgraph`)

- `JanusGraphDriver implements IDriver` — schema via `JanusGraphManagement` (vertex labels, property keys; references
  become edge labels), entities are ByteBuddy subclasses of `VertexWrapper` (wraps a TinkerPop `Vertex`/`Element`).
  Queries: `polyglot.properties` supplies Gremlin per query id; otherwise only a basic SQL → Gremlin translation.
- `JanusGraphUtils` — property-type checks, element/type helpers.

## Dependencies

- `janusgraph-core` 1.1.0 + `gremlin-core` 3.7.x (compile), `janusgraph-inmemory` (test). No `--add-opens` needed by
  the tests (same results with and without them on JDK 21/25, so they were removed in Stage A11).

## Tests

- `JanusGraphUniversalTest` (20) on the `inmemory` backend.
- Baseline 2026-09-25 (JDK 21): 6 of 20 fail (`testAutoWrapping`, `testAutoUnwrapping`, `testDAOQuery`, `testCommand`,
  `testLookupInDAO`, `testLookupInEntity`) — incomplete query translation/wrapping, not a JDK problem. The 2 Checkstyle
  violations of the baseline are fixed (`package-info.java`, private `JanusGraphUtils` constructor).
