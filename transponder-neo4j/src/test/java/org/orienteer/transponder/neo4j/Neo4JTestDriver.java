package org.orienteer.transponder.neo4j;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.orienteer.transponder.ITestDriver;

public class Neo4JTestDriver extends Neo4JDriver implements ITestDriver{
	
	public Neo4JTestDriver(GraphDatabaseService database, Transaction externalTransaction) {
		super(database, externalTransaction);
	}

	public Neo4JTestDriver(GraphDatabaseService database) {
		super(database);
	}

	@Override
	public boolean hasType(String typeName) {
		return true;
	}

	@Override
	public boolean hasProperty(String typeName, String propertyName) {
		return true;
	}

	@Override
	public boolean hasReferenceProperty(String typeName, String propertyName, String referenceType) {
		return true;
	}

	@Override
	public boolean hasIndex(String typeName, String indexName, String... properties) {
		return true;
	}

	@Override
	public Object createSeedObject(String typeName, Map<String, ?> properties) {
		try(TransactionHolder holder = new TransactionHolder()) {
			Node node = holder.getTransaction().createNode(Label.label(typeName));
			for (Map.Entry<String, ?> entry : properties.entrySet()) {
				node.setProperty(entry.getKey(), entry.getValue());
			}
			return node;
		}
	}
	

}
