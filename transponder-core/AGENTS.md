# transponder-core

The engine: `org.orienteer.transponder:transponder-core` jar **plus test-jar** (the test-jar is the driver-independent
suite every driver module reuses). Orienteer gets it transitively via `transponder-orientdb`.
Root rules: [`../AGENTS.md`](../AGENTS.md); plan: [`../REFRESH_PLAN.md`](../REFRESH_PLAN.md).

## Key classes (`org.orienteer.transponder`)

- `Transponder` — public API. `getProxyClass(...)` builds a ByteBuddy subclass of the driver's base class (or of a
  concrete DAO class) implementing the requested interfaces, named `transponder.<dialect>.<EntityType|dao.Simple>`,
  cached in a static `TypeCache` keyed by driver cache key + classes + `ProxyType`. `define(...)` walks `@EntityType`
  interfaces (super-interfaces first) and calls the `IDriver` schema methods through a postponing `DescribeContext`.
- `IDriver` — SPI: schema (`createType`, `createProperty`, `setupRelationship`, `createIndex`, `onPostCreateType`),
  instances (`newEntityInstance`, `wrapEntityInstance`, `toSeed`, `replaceSeed`), queries (`query`, `querySingle`, `command`),
  `getDialect`, `getMutator`. Several are `default` methods — adding abstract ones breaks Orienteer's `OrienteerDriver`.
- `ProxyType` (ENTITY/DAO/DELEGATE) → `mutator.StackedMutator` roots; `mutator/*` (`GetterMutator`, `SetterMutator`,
  `QueryMutator`, `LookupMutator`, `CommandMutator`, `DelegatorMutator`, `AnnotationMutator`) + `BuilderScheduler`
  (matches methods once, applies ByteBuddy `intercept`s in priority order).
- `annotation/*` — `@EntityType`, `@EntityProperty`, `@EntityIndex(es)`, `@EntityPropertyIndex`, `@Query`, `@Lookup`,
  `@Command`, `@DefaultValue`, `@AdviceAnnotation`, `@DelegateAnnotation`, `@OverrideByThis`, `common.@Sudo`,
  `binder.*` (ByteBuddy parameter binders).
- `polyglot.DefaultPolyglot` — `<dialect>.<queryId>[.language]` overrides from every
  `/META-INF/transponder/polyglot.properties` on the class path.
- `CommonUtils` (`@UtilityClass`) — type helpers, `toMap`, `OBJENESIS`, `listDeclaredMethods` (source order via
  ByteBuddy's repackaged ASM `ClassReader`).

## Dependencies

- Compile: ByteBuddy, Objenesis (both inherited from the parent), Guava. `provided`: Lombok.
- ByteBuddy (`IMutator`, `BuilderScheduler`, `ElementMatcher`, `Advice`) and Guava are part of the public API.

## Tests

- `CoreUniversalTest` (20, in-memory `TestDriver`), `CoreSpecificTest` (9), `ByteBuddyTest` (5), `UtilsTest` (3).
- Test-jar content: `AbstractUniversalTest`, `ITestDriver` (JUnit 4 `Assert`), `TestDriver`, `datamodel/*`.
  Changing it changes every driver's suite.

## Pitfalls

- `listDeclaredMethods` must keep reading class files the JDK produces (Java 21 target, JDK 25 runtime) — schema
  property order depends on it; `DAOTest.testProperMethodListOrder` and `UtilsTest` (sealed interface) cover it.
  Keep its `ClassVisitor` at the newest `Opcodes.ASM*` level: lower levels throw on newer attributes.
- Generated class names are fixed per entity type; `additionalInterfaces` add a random `$suffix`.
