# transponder-neo4j

Neo4j driver (`org.orienteer.transponder:transponder-neo4j`), embedded Neo4j 4.4. **Not used by Orienteer**; kept
under the driver policy (plan D7: fixes within the same DB major, otherwise the `parked` profile).
Root rules: [`../AGENTS.md`](../AGENTS.md); plan: [`../REFRESH_PLAN.md`](../REFRESH_PLAN.md).

**Parked** (profile `parked`, plan D13): not built by default, never published. Try it with `./mvnw -Pparked -pl transponder-core,transponder-neo4j verify`.

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
- On JDK 21+ the DB doesn't start: Neo4j 4.4's `UnsafeUtil.initDirectByteBuffer` sets the now-`final`
  `java.nio.Buffer.capacity` via a `VarHandle` (`UnsupportedOperationException: set`); 4.4.48 is the same. No flag
  fixes it; Neo4j 5.26 LTS / 2026.x would be a DB-major port (plan X).
