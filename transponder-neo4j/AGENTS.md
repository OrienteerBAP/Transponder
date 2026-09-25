# transponder-neo4j

Neo4j driver (`org.orienteer.transponder:transponder-neo4j`), embedded Neo4j 4.4. **Not used by Orienteer**; kept
under the driver policy (plan D7: fixes within the same DB major, otherwise the `parked` profile).
Root rules: [`../AGENTS.md`](../AGENTS.md); plan: [`../REFRESH_PLAN.md`](../REFRESH_PLAN.md).

## Key classes (`org.orienteer.transponder.neo4j`)

- `Neo4JDriver implements IDriver` — bound to a `GraphDatabaseService` (+ optional `Transaction`); types are labels,
  references are relationships; queries are Cypher (`polyglot.properties` in tests shows the dialect).
- `EntityWrapper` — base class of generated wrappers, covers nodes and relationships; `@Neo4JRelationship` marks
  interfaces mapped to relationships; `Neo4JUtils` (`@UtilityClass`).

## Dependencies

- `org.neo4j:neo4j` 4.4.x (compile; the full embedded server). Surefire needs
  `--add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED`.

## Tests

- `Neo4JUniversalTest` (20): embedded DB under `target/db`.
- Baseline 2026-09-25: on JDK 21 the DB doesn't start (`UnsupportedOperationException: set` from Neo4j 4.4's
  off-heap `ByteBuffer` wrapping). Neo4j 5.26 LTS / 2026.x would be a DB-major port (plan X).
