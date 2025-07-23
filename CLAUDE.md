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
- `transponder-orientdb/` - OrientDB driver (`ODriver.java`)
- `transponder-arcadedb/` - ArcadeDB driver (`ArcadeDBDriver.java`)
- `transponder-neo4j/` - Neo4j driver (`Neo4JDriver.java`)
- `transponder-mongodb/` - MongoDB driver (`MongoDBDriver.java`) - uses MongoDB Java Driver 5.2.1

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

## Development Notes

- Java 8 target (maven.compiler.source/target = 1.8)
- Uses Lombok for boilerplate reduction
- ByteBuddy for runtime class generation
- Google Guava for utilities
- Checkstyle enforced with custom rules (check_style.xml)
- Multi-module Maven project with parent POM dependency management