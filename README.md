[![Java CI](https://github.com/OrienteerBAP/Transponder/actions/workflows/maven.yml/badge.svg)](https://github.com/OrienteerBAP/Transponder/actions/workflows/maven.yml) [![Build Status](https://app.travis-ci.com/OrienteerBAP/Transponder.svg?branch=master)](https://app.travis-ci.com/OrienteerBAP/Transponder)

![Transponder_Logo](https://user-images.githubusercontent.com/1199285/138948483-27e0ad55-15c3-4eef-b39e-94b68c37660e.png)

# Transponder
Transponder is an Object Relational Mapping(ORM) library for NoSQL databases. It's lightweight, with very small memory footprint.
Transponder [dynamically generates bytecode](https://github.com/raphw/byte-buddy) over native DB client classes, so there are NO overheads for reflection and NO double storing of data.

## Content

1. [Use Cases](#use-cases)
2. [Key Benefits](#key-benefits)
3. [NoSQL Database Support Status](#nosql-database-support-status)
4. [Java Version Requirements](#java-version-requirements)
5. [Getting Started](#getting-started)
6. [Defining DataModel](#defining-datamodel)
7. [Transponder API](#transponder-api)
8. [Transponder Annotations](#transponder-annotations)
9. [Support of Multiple Dialects](#support-of-multiple-dialects)
10. [Comparison With Other ORMs](#comparison-with-other-orms)
11. [Suppport](#suppport)

### Use Cases
Transponder can be used for
* Defining a data model in Java source code
* Automatic creation of a datamodel in a NoSQL DB
* Generation of Data Access Objects (DAO) - utility classes to work with data
* Easy customization for specific needs, for example: introduce custom annotation `@Sudo` to execute code under priviledged access

### Key Benefits

* Lightweight
* Small memory footprint
* Transferable: datamodel defined for one DB can be reused for another one
* Small learning curve

### NoSQL Database Support Status

| Database | Status | Type | Complexity | Java API Quality | Community | Performance | Maven Dependency |
|----------|--------|------|------------|------------------|-----------|-------------|-------------------|
| **Currently Supported** |
| [OrientDB](https://github.com/orientechnologies/orientdb) | ✅ | Multi-Model | Medium | Excellent | Strong | High | `org.orienteer.transponder:transponder-orientdb` |
| [ArcadeDB](https://github.com/ArcadeData/arcadedb) | ✅ | Multi-Model | Medium | Good | Growing | High | `org.orienteer.transponder:transponder-arcadedb` |
| [Neo4j](https://github.com/neo4j/neo4j) | ✅ | Graph | Medium | Excellent | Very Strong | High | `org.orienteer.transponder:transponder-neo4j` |
| [JanusGraph](https://github.com/JanusGraph/janusgraph) | ✅ | Graph | Low | Excellent | Strong | High | `org.orienteer.transponder:transponder-janusgraph` |
| [MongoDB](https://github.com/mongodb/mongo) | ✅ | Document | Medium | Good | Very Strong | High | `org.orienteer.transponder:transponder-mongodb` |
| **High Priority - Recommended Next** |
| [ArangoDB](https://github.com/arangodb/arangodb) | 🔄 | Multi-Model | Medium | Good | Very Strong | High | `org.orienteer.transponder:transponder-arangodb` |
| [CouchDB](https://github.com/apache/couchdb) | 🔄 | Document | Low | Good | Strong | Medium | `org.orienteer.transponder:transponder-couchdb` |
| **Medium Priority - Strategic Extensions** |
| [Redis](https://github.com/redis/redis) | 📋 | Key-Value | Low | Excellent | Very Strong | Very High | `org.orienteer.transponder:transponder-redis` |
| [Memgraph](https://github.com/memgraph/memgraph) | 📋 | Graph | Very Low | Excellent | Growing | Very High | `org.orienteer.transponder:transponder-memgraph` |
| **Lower Priority - Future Consideration** |
| [Apache Cassandra](https://github.com/apache/cassandra) | 📋 | Wide Column | High | Excellent | Very Strong | High | `org.orienteer.transponder:transponder-cassandra` |
| [Couchbase](https://github.com/couchbase) | 📋 | Document | Medium | Good | Strong | High | `org.orienteer.transponder:transponder-couchbase` |
| [Hazelcast](https://github.com/hazelcast/hazelcast) | 📋 | Key-Value/Cache | Medium | Excellent | Strong | Very High | `org.orienteer.transponder:transponder-hazelcast` |
| [DynamoDB](https://aws.amazon.com/dynamodb/) | 📋 | Key-Value/Document | High | Good | Strong | High | `org.orienteer.transponder:transponder-dynamodb` |

**Legend:**
- ✅ **Currently Supported** - Ready to use
- 🔄 **High Priority** - Recommended for next implementation based on similarity to existing drivers and ecosystem fit
- 📋 **Planned** - Future consideration based on community demand and strategic value

Please create an [issue](https://github.com/OrienteerBAP/Transponder/issues) or [discussion](https://github.com/OrienteerBAP/Transponder/discussions) if you need support of some other DBs or expedite priority for those which are not yet supported.

## Java Version Requirements

**Important**: Different Transponder modules have specific Java version requirements based on their underlying database dependencies:

- **transponder-core**: Java 8+ (tested up to Java 21)
- **transponder-orientdb**: Java 8+ (OrientDB 3.2.36 works with Java 8-21)
- **transponder-arcadedb**: Java 11+ (ArcadeDB 23.12.1 requirement, works with Java 17+)
- **transponder-neo4j**: Java 8-17 (Neo4j 4.4.38 fails with Java 21)
- **transponder-janusgraph**: Java 8-11 (JanusGraph 1.1.0 limitation - requires explicit Java 11 compiler config)
- **transponder-mongodb**: Java 8+ (MongoDB Java Driver 5.2.1 works with all Java versions)

### Tested Java Version Compatibility Matrix

| Module | Java 8 | Java 11 | Java 17 | Java 21 | Notes |
|--------|---------|---------|---------|---------|-------|
| **transponder-core** | ⚠️* | ✅ | ✅ | ✅ | *Requires Maven compiler config changes for Java 8 |
| **transponder-orientdb** | ⚠️* | ✅ | ✅ | ✅ | *Requires Maven compiler config changes for Java 8 |
| **transponder-arcadedb** | ❌ | ✅ | ✅ | ✅ | Minimum Java 11 required |
| **transponder-neo4j** | ⚠️* | ✅ | ✅ | ❌ | *Requires Maven compiler config changes for Java 8; **Fails on Java 21** |
| **transponder-janusgraph** | ⚠️* | ✅ | ❌ | ❌ | *Requires Maven compiler config changes for Java 8; Uses Java 11 compiler config |
| **transponder-mongodb** | ⚠️* | ✅ | ✅ | ✅ | *Requires Maven compiler config changes for Java 8 |

### Recommended Java Versions by Use Case

#### For Maximum Compatibility
Use **Java 11** - all modules work perfectly with Java 11.

#### For Latest Java Features  
Use **Java 17** with modules: core, orientdb, arcadedb, mongodb (excludes neo4j and janusgraph).

#### Production Environments
- **Java 11**: Safest choice, all modules tested and working
- **Java 17**: Good choice if not using Neo4j or JanusGraph  
- **Java 21**: Limited to core, orientdb, arcadedb, mongodb modules only

### JanusGraph Distributed Graph Database

The JanusGraph module provides enterprise-grade distributed graph database capabilities that scale beyond Neo4j limitations:

- **Massive Scalability**: Handle graphs with billions of vertices and edges across multiple machines
- **Multiple Storage Backends**: Choose from Cassandra, HBase, or BerkeleyDB for storage
- **Native TinkerPop Integration**: Full Apache TinkerPop 3.7.3 and Gremlin query support
- **ACID Transactions**: Complete transactional support for data consistency
- **High Availability**: Built-in replication and fault tolerance

Use JanusGraph when you need distributed graph processing or when your graph data exceeds single-machine capabilities.

For development environments with multiple Java versions, consider using tools like `jenv` or `SDKMAN!` to manage Java versions per project.

## Getting Started

**Before you start**: Check the [Java Version Requirements](#java-version-requirements) section above to ensure your environment meets the requirements for your target database.

Add the following dependency into your `pom.xml`:

```xml
<dependency>
   <groupId>org.orienteer.transponder</groupId>
   <artifactId>transponder-${NOSQL DB NAME}</artifactId>
   <version>${project.version}</version>
</dependency>
```

If you are using `SNAPSHOT` version, please make sure that the following repository is included into your `pom.xml`:

```xml
<repository>
	<id>Sonatype Nexus</id>
	<url>https://oss.sonatype.org/content/repositories/snapshots/</url>
	<releases>
		<enabled>false</enabled>
	</releases>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
</repository>
```

## Defining DataModel

Use interface class with annotation `@EntityType("EntryTypeName")` to define a type from your data-model. All getters and setters will be mapped to corresponding properties/columns of an entity in a database. You can use interface **default** methods for your custom methods. There is a set of annotations supported by Transponder to simplify work with the data model: `@EntityIndex`, `@Query`, `@Lookup`, `@Command` and etc. Please see corresponding chapter for details. Lets create simple datamodel to mimic file-system:

```java
@EntityType("Entry")
@EntityIndex(name = "nameParent", properties = {"name", "parent"})
public interface IEntry {

	public String getName();
	public void setName(String value);
	
	public IFolder getParent();
	public void setParent(IFolder value);
	
	@Lookup("select from Entry where name=:name and parent=:parent")
	public boolean lookupByName(String name, IFolder parent);
  
  	public default String getFullPath() {
		IFolder parent = getParent();
		return (parent!=null?parent.getFullPath():"")+"/"+getName();
	}
}

@EntityType("Folder")
public interface IFolder extends IEntry {

	public List<IEntry> getChild();
	public void setChild(List<IEntry> value);
}

@EntityType("File")
public interface IFile extends IEntry {

	public byte[] getContent();
	public void setContent(byte[] value);
	
}
```
After calling `transponder.define(IFile.class, IFolder.class)` Transponder will create the following datamodel in a database:
![image](https://user-images.githubusercontent.com/1199285/139164433-88eb9612-447e-4061-b452-c0035dcf6d37.png)

Additionally you can create Data Access Object to be able to query/modify your data:

```java
public interface IFileSystem {
	
	@Query("select from Folder where name = :name and parent is null")
	public IFolder getRoot(String name);
	
	@Query("select from Entry where name=:name and parent=:parent")
	public IEntry lookupByName(String name, IFolder parent);
	
	@Query("select from Entry where name like :search")
	public List<IEntry> search(String search);
	
	@Command("delete from File where content is null")
	public void removeEmpty();
}
```

## Transponder API

To create **Transponder** instance:
```java
Transponder transponder = new Transponder(driver);
//For example:
Transponder transponder = new Transponder(new ODriver()); //OrientDB
Transponder transponder = new Transponder(new ArcadeDBDriver(arcadeDatabase)); //ArcadeDB
```

To define datamodel in a database:
```java
transponder.define(IFolder.class, IFile.class, IMyOtherEntity.class, ...);
```

To create DAO instance from the specified interface or class:
```java
IFileSystem fsDao = transponder.dao(IFileSystem.class);
IFileSystem fsDao = transponder.dao(IFileSystem.class, IOtherDAOClass.class, IYetAnotherDAOClass.class, ...);
```

After DAO creation you can use it right away:
```java
List<IEntry> textFiles = fsDao.search("%.txt");
```

To create new wrapped entity:
```java
IFolder folder = transponder.create(IFolder.class);
IFolder folder = transponder.create(IFolder.class, IMyOtherUsefullWrapper.class, ...);
```

After creation of an entity you can work with it as usual java object:
```java
folder.setName("My Cool Folder");
folder.setParent(myOtherFolder);
String fullPath = folder.getFullPath();
```

To persist/save entity into DB:

```java
Tranponder.save(folder);
```

Or you can do simply `folder.save()` if you mixin the following method into your wrapper:

```
public default IEntry save() {
  Transponder.save();
  return this;
}
```

Also you can wrap some existing entity from a database into wrapped one. Example for OrientDB:
```java
ODocument myFolderDoc = ...;
IFolder folder = transponder.provide(myFolderDoc, IFolder.class);
IFolder folder = transponder.provide(myFolderDoc, IFolder.class, IMyOtherUsefulWrapper.class, ...); //To mixin other useful interfaces
IFodler folder = transponder.wrap(myFolderDoc); //More generic version, but corresponding wrapper should be defined by transponder.define(...) in this case
```

If needed, you can unwrap entity as well. Example for OrientDB:
```java
ODocument myFolderDoc = (ODocument)Transponder.unwrap(folder);
```

Sometimes it's useful to rewrap the same entity but into different class.
```java
MyNewWrapper newWrapper = Transponder.rewrap(oldWrapper, MyNewWrapper.class, SomeOtherInterface.class, ...);
```

Also you can upgrade existing wrapper by adding more interfaces to be supported
```java
MyEntity myEntity = ...;
myEntity = Transponder.upgrade(myEntity, SomeNewInterface.class, SomeOtherInterface.class);
```

If you have wrapped entity and you need obtain Transponder:
```java
Transponder transponder = Transponder.getTransponder(folder);
```

## Transponder Annotations

| Annotation | Description |
|------------|-------------|
|`@EntityType`|Current class defines entity type|
|`@EntityIndex/@EntityIndexes`|Creates indexes on the entity type in a database|
|`@EntityProperty`|This getter/setter method should be mapped to corresponding property of an entity in a database|
|`@EntityPropertyIndex`|Creates index in a datasource on current property|
|`@Query`|Method execute query in a database and return corresponding result|
|`@Lookup`|Lookup database for an entity according to search criterias and if found: replace underling entity of the current wrapper|
|`@Command`|Execute some command in a database|
|`@DefaultValue`|Return provided default value if actual result from this method is null|
|`@OrientDBProperty`|OrientDB specific additional settings for the property|
|`@ArcadeDBProperty`|ArcadeDB specific additional settings for the property|

Annotations in bytecode generation within Transponder is very flexible (due to [Byte Buddy](https://github.com/raphw/byte-buddy)) and can be easily extended to support custom cases. For example: `@Sudo` - to execute some method under super user, `@Count` - to count number of invokations for metrics and etc.

### Support of Multiple Dialects
Queries and commands for the same functions might vary for different databases. Valid SQL for one NoSQL database, might require correction for another one. That's why **Transponder** supports polyglot definitions for `@Query`, `@Lookup` and `@Command`. **Transponder** do translation to corresponding dialect during dynamic generation of a wrapper, so there is no overheads during actual runtime. Every query/command has **id**. It's either can be defined manually (for example `@Query(id="myQuery", value="select ...")`) or generated automatically (for example first query for `IFileSystem` above will have id `<fullpackagename>.IFileSystem.getRoot`. Then **Transponder** uses provided resource file by path `/META-INF/transponder/polyglot.properties` to lookup proper query for a specific dialect. For example, for query with id `myQuery` for OrientDB library will look for keys `orientdb.myQuery` and `orientdb.myQuery.language`. If first one is found - it will be used as actual query for OrientDB. If second one was also found: correspinding language will overload language defined in actual annotation.

## Comparison With Other ORMs

|              | **Transponder** | **Spring Data** | **Hibernate** | **MyBatis** |
|--------------|-----------------|-----------------|---------------|-------------|
|NoSQL Support|:white_check_mark:|:white_check_mark:|:white_check_mark:|:x:|
|Universal DataModel Markup|:white_check_mark:|:x:|:x:|:x:|
|No Double Caching|:white_check_mark:|:x:|:x:|:x:|
|Easy Extendability On User Level Code|:white_check_mark:|:x:|:x:|:x:|
|Easy SPI For New Drivers|:white_check_mark:|:x:|:x:|:x:|
|Support of Multi-Inheritance|:white_check_mark:|:x:|:x:|:x:|

**Disclaimer**: Comparison might be subjective. If you have comments: please create an issue or discussion

## Suppport

If you need any support or questions please create an [issue](https://github.com/OrienteerBAP/Transponder/issues) or [discussion](https://github.com/OrienteerBAP/Transponder/discussions).
