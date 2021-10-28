[![Java CI](https://github.com/OrienteerBAP/Transponder/actions/workflows/maven.yml/badge.svg)](https://github.com/OrienteerBAP/Transponder/actions/workflows/maven.yml) [![Build Status](https://app.travis-ci.com/OrienteerBAP/Transponder.svg?branch=master)](https://app.travis-ci.com/OrienteerBAP/Transponder)

![Transponder_Logo](https://user-images.githubusercontent.com/1199285/138948483-27e0ad55-15c3-4eef-b39e-94b68c37660e.png)

# Transponder
Transponder is an Object Relational Mapping(ORM) library for NoSQL databases. It's lightweight, with very small memory footprint.
Transponder [dynamically generates bytecode](https://github.com/raphw/byte-buddy) over native DB client classes, so there are NO overheads for reflection and NO double storing of data.

### Can be used for
* Defining in Java source code a datamodel
* Automatic creation of a datamodel in a NoSQL DB
* Generation of Data Access Objects (DAO) - utility classes to work with your data
* Easy customization per your specific need, for example: introduce custom annotation `@Sudo` to execute some code under priviledged access

### Key Benefits

* Lightweight
* Small memory footprint
* Transferable: datamodel defined for one DB can be reused for another one
* Small learning curve

### Supported NoSQL Databases

- [X] [OrientDB](https://github.com/orientechnologies/orientdb)
- [X] [ArcadeDB](https://github.com/ArcadeData/arcadedb)

### To be supported soon

- [ ] [Neo4J](https://github.com/neo4j/neo4j)
- [ ] [ArangoDB](https://github.com/arangodb/arangodb)
- [ ] [Cassandra](https://github.com/apache/cassandra)
- [ ] [Hazelcast](https://github.com/hazelcast/hazelcast)
- [ ] [Couchbase](https://github.com/couchbase)
- [ ] [MongoDB](https://github.com/mongodb/mongo)
- [ ] [DynamoDB](https://aws.amazon.com/dynamodb/)

Please create an [issue](https://github.com/OrienteerBAP/Transponder/issues) or [discussion](https://github.com/OrienteerBAP/Transponder/discussions) if you need support of some other DBs or expedite priority for those which are not yet supported.

## Getting Started

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

## Defining Data-Model

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
	
	@Command("delete from File where content is null")
	public void removeEmpty();
}
```
