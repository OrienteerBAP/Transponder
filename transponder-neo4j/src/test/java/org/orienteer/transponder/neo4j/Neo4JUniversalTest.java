package org.orienteer.transponder.neo4j;

import java.nio.file.Paths;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.orienteer.transponder.AbstractUniversalTest;

public class Neo4JUniversalTest extends AbstractUniversalTest {
	private static DatabaseManagementService managementService = new DatabaseManagementServiceBuilder( Paths.get("target", "db")).build();
	private static GraphDatabaseService database = managementService.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
	
	public Neo4JUniversalTest() {
		super(new Neo4JTestDriver(database, database.beginTx()));
	}
}
