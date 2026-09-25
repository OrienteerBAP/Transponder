# transponder-orientdb

The OrientDB driver: `org.orienteer.transponder:transponder-orientdb` — **the artifact Orienteer depends on**.
Root rules: [`../AGENTS.md`](../AGENTS.md); plan: [`../REFRESH_PLAN.md`](../REFRESH_PLAN.md).

## Key classes (`org.orienteer.transponder.orientdb`)

- `ODriver implements IDriver` — schema on `OSchema` (classes, properties, indexes; custom attribute
  `transponder.wrapper` = main interface name, used by `getEntityMainClass`), entities are ByteBuddy subclasses of
  OrientDB's `ODocumentWrapper`, queries via the thread-bound `ODatabaseSession` (`ODatabaseRecordThreadLocal`).
  Static helpers `asWrapper`, `asDocument`, `save`, `reload`. **Orienteer's `OrienteerDriver` extends it** and overrides
  `createType`, `onPostCreateType`, `createProperty`, `setupRelationship`, `getEntityMainClass`: keep those signatures,
  and the protected `getSession`/`getSchema`.
- `IODocumentWrapper` — interface mirroring `ODocumentWrapper` methods so DAO interfaces can call `save()`, `reload()`,
  `getDocument()`…; used by 15 Orienteer files.
- `@OrientDBProperty` — OrientDB-specific property settings (type, linked type, notNull, mandatory, min/max, collate…).
- `advice.SudoAdvice` — ByteBuddy `Advice` behind `@Sudo`: runs the method on a no-auth DB session.

## Dependencies

- `transponder-core` (compile); `orientdb-core` **`provided`** — Orienteer decides the runtime OrientDB version.
- OrientDB 3.2.x brings GraalVM/Truffle 21.3.5, which crashes embedded OrientDB on JDK 22+. The tests replace it with
  GraalJS 25.0.4 in `test` scope (plan D4); nothing Graal-related leaks to consumers.

## Tests

- `DAOTest` (13) and `OrientDBUniversalTest` (20): embedded in-memory DBs under `embedded:target/` (default users
  admin/reader), no network ports. `DAOTest.testSudo` checks `@Sudo` as `reader`.
- Test fixtures: `IDAO*` interfaces, `ITestDAO`, `OTestDriver`, `polyglot.properties`.

## Pitfalls

- A closed `OResultSet` yields nothing on OrientDB 3.2.56: `query`/`querySingle`/`command` collect inside the
  try-with-resources — keep it that way.
- Every call needs an active DB session on the current thread (`getSession` throws otherwise).
