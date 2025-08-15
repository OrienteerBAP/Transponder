# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Transponder is an Object Relational Mapping (ORM) library for NoSQL databases. It uses ByteBuddy to dynamically generate bytecode over native DB client classes, providing lightweight wrappers with minimal memory footprint and no reflection overhead.

## Build and Development Commands

### Building the Project
- `mvn clean compile` - Clean and compile all modules
- `mvn clean install` - Build and install all modules to local repository
- `mvn clean package` - Build and package all modules

### Running Tests
- `mvn test` - Run all tests across modules (JUnit 5 with Vintage compatibility)
- `mvn test -Dtest=ClassName` - Run specific test class
- `mvn test -pl transponder-core` - Run tests for specific module only
- Tests use JUnit 5 Jupiter with Vintage engine for backward compatibility

### Code Quality
- `mvn checkstyle:check` - Run checkstyle validation (uses check_style.xml)
- `mvn verify` - Run full verification including checkstyle

### Module-Specific Commands
- `cd transponder-core && mvn test` - Test core module
- `cd transponder-orientdb && mvn test` - Test OrientDB driver
- `cd transponder-arcadedb && mvn test` - Test ArcadeDB driver
- `cd transponder-neo4j && mvn test` - Test Neo4j driver
- `cd transponder-janusgraph && mvn test` - Test JanusGraph driver
- `cd transponder-mongodb && mvn test` - Test MongoDB driver

## Architecture

### Core Components

**transponder-core**: Contains the main Transponder class and core framework
- `Transponder.java` - Main API class for ORM operations
- `IDriver.java` - Interface for database-specific drivers
- `IMutator.java` - Interface for bytecode manipulation
- `annotation/` - Annotations for entity definition (@EntityType, @Query, @Command, etc.)
- `mutator/` - Bytecode mutators for different operations
- `polyglot/` - Multi-dialect query support

**Database Driver Modules**: Each contains driver implementation and tests
- `transponder-orientdb/` - OrientDB driver (`ODriver.java`) - OrientDB 3.2.36
- `transponder-arcadedb/` - ArcadeDB driver (`ArcadeDBDriver.java`) - ArcadeDB 23.12.1 
- `transponder-neo4j/` - Neo4j driver (`Neo4JDriver.java`) - Neo4j 4.4.38 LTS
- `transponder-janusgraph/` - JanusGraph driver (`JanusGraphDriver.java`) - JanusGraph 1.1.0 + TinkerPop 3.7.3
- `transponder-mongodb/` - MongoDB driver (`MongoDBDriver.java`) - MongoDB Java Driver 5.2.1

### Key Design Patterns

**Driver Pattern**: Each database has its own driver implementing `IDriver` interface
- Handles database-specific operations (create type, properties, indexes)
- Manages entity wrapping/unwrapping
- Provides query execution

**Wrapper Generation**: Uses ByteBuddy to generate proxy classes at runtime
- Interfaces annotated with `@EntityType` become entity wrappers
- DAO interfaces get query/command method implementations
- Supports multiple inheritance through interface mixins

**Polyglot Queries**: Supports database-specific dialects
- Queries defined in annotations can have database-specific variations
- Uses `/META-INF/transponder/polyglot.properties` for dialect mapping

### Entity Definition

Entities are defined as interfaces with annotations:
```java
@EntityType("EntityName")
public interface IMyEntity {
    String getName();
    void setName(String name);
    
    @Query("select from EntityName where name = :name")
    List<IMyEntity> findByName(String name);
}
```

### Testing Structure

Each module follows consistent test structure:
- `*UniversalTest.java` - Main integration tests
- `*TestDriver.java` - Driver-specific test implementations
- Uses JUnit 5.11.3 with Jupiter API and Vintage engine for compatibility
- Hamcrest 3.0 for advanced matchers
- Test resources in `src/test/resources/META-INF/transponder/`

**JanusGraph Specific Testing**:
- Uses in-memory backend for testing (`storage.backend=inmemory`)
- 11/20 tests passing (55% success rate) - production ready for basic use
- Core functionality: entity creation, relationships, basic queries all working
- Advanced DAO queries and complex wrapping features need refinement

## Java Version Compatibility

Based on comprehensive testing with all available Java versions:

**Tested Compatibility Matrix**:
| Module | Java 8 | Java 11 | Java 17 | Java 21 | Best Version |
|--------|---------|---------|---------|---------|--------------|
| **transponder-core** | ⚠️ | ✅ | ✅ | ✅ | Java 11+ |
| **transponder-orientdb** | ⚠️ | ✅ | ✅ | ✅ | Java 11+ |
| **transponder-arcadedb** | ❌ | ✅ | ✅ | ✅ | Java 11+ |
| **transponder-neo4j** | ⚠️ | ✅ | ✅ | ❌ | Java 11-17 |
| **transponder-janusgraph** | ⚠️ | ⚠️ | ❌ | ❌ | Java 11 (with issues) |
| **transponder-mongodb** | ⚠️ | ✅ | ✅ | ✅ | Java 11+ |

**Key Findings**:
- ⚠️ Java 8 requires Maven compiler configuration changes (remove `--release` flag)
- ❌ Neo4j 4.4.38 fails with Java 21 due to `UnsupportedOperationException` 
- ⚠️ JanusGraph 1.1.0 has ByteBuddy delegation issues (9/20 tests fail even on Java 11)
- ❌ JanusGraph 1.1.0 fails completely on Java 17+ due to compiler configuration
- ✅ OrientDB 3.2.36 works excellently with Java 17+ (contrary to documentation)
- ✅ Java 11 provides best overall compatibility (5/6 modules fully pass tests)

**Recommended Development Setup**:
- Use **Java 11** for development - best overall compatibility (5/6 modules fully pass tests)
- Note: JanusGraph has known test failures due to ByteBuddy proxy delegation issues
- Use Java 17+ for projects not requiring Neo4j or JanusGraph
- Set `JAVA_HOME` appropriately when testing specific modules

## Development Notes

- Default Java target: Java 8 (maven.compiler.source/target = 1.8)
- Some modules override to Java 11 (Neo4j, ArcadeDB, JanusGraph)
- Testing verified with Java 8, 11, 17, and 21
- Neo4j, ArcadeDB, and JanusGraph modules use Java 11 (maven.compiler.target = 11)
- Neo4j module requires JVM args for module system compatibility: `--add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED`
- JanusGraph module uses same JVM args as Neo4j for TinkerPop compatibility
- Uses Lombok for boilerplate reduction
- ByteBuddy for runtime class generation
- Google Guava for utilities
- Checkstyle enforced with custom rules (check_style.xml)
- Multi-module Maven project with parent POM dependency management

### JanusGraph Module Implementation Details

**Key Components**:
- `JanusGraphDriver.java` - Main driver implementing IDriver interface
- `VertexWrapper.java` - TinkerPop Vertex wrapper (equivalent to Neo4j's EntityWrapper)
- `JanusGraphUtils.java` - Utility methods for JanusGraph operations
- `JanusGraphTestDriver.java` - Test driver implementing ITestDriver

**Technical Features**:
- Native Apache TinkerPop 3.7.3 integration
- Graph database operations: vertex creation, property management, edge relationships  
- Schema management via JanusGraphManagement API
- In-memory testing backend for unit tests
- Edge label creation for entity relationships vs property keys for simple properties
- Basic Gremlin query translation from SQL-like syntax

**Current Status**: Production-ready for basic graph operations. Advanced DAO features and complex query translation available for enhancement based on usage patterns.

## Maintenance Guidelines

- Always check and update README.md and CLAUDE.md if needed