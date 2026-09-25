# transponder-mongodb

MongoDB driver (`org.orienteer.transponder:transponder-mongodb`, never published so far). **Not used by Orienteer**;
kept under the driver policy (plan D7: fixes within the same major, otherwise the `parked` profile).
Root rules: [`../AGENTS.md`](../AGENTS.md); plan: [`../REFRESH_PLAN.md`](../REFRESH_PLAN.md).

## Key classes (`org.orienteer.transponder.mongodb`)

- `MongoDBDriver implements IDriver` — bound to a `MongoDatabase`; types are collections; entities are ByteBuddy
  subclasses of `TransponderDocument` (a `org.bson.Document` that remembers its collection). Queries are JSON command
  documents (`{$operation: "find", $collection: …, $filter: …}`, see the test `polyglot.properties`).
- `MongoDBUtils` (`@UtilityClass`).

## Dependencies

- `mongodb-driver-sync` 5.x (compile); flapdoodle `de.flapdoodle.embed.mongo` 4.x (test).

## Tests

- `MongoDBUniversalTest` (20): flapdoodle downloads a MongoDB 5.0 server binary once (into `~/.embedmongo`) and starts
  it on a free port — the first run needs network access.
