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

## Supported NoSQL Databases

- [X] [OrientDB](https://github.com/orientechnologies/orientdb)
- [X] [ArcadeDB](https://github.com/ArcadeData/arcadedb)

## To be supported soon

- [ ] [Neo4J](https://github.com/neo4j/neo4j)
- [ ] [ArangoDB](https://github.com/arangodb/arangodb)
- [ ] [Cassandra](https://github.com/apache/cassandra)
- [ ] [Hazelcast](https://github.com/hazelcast/hazelcast)
- [ ] [Couchbase](https://github.com/couchbase)
- [ ] [MongoDB](https://github.com/mongodb/mongo)
- [ ] [DynamoDB](https://aws.amazon.com/dynamodb/)

Please create an [issue](https://github.com/OrienteerBAP/Transponder/issues) or [discussion](https://github.com/OrienteerBAP/Transponder/discussions) if you need support of some other DBs or expedite priority for those which are not yet supported.
