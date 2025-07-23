package org.orienteer.transponder.neo4j;

import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.orienteer.transponder.AbstractUniversalTest;

public class Neo4JUniversalTest extends AbstractUniversalTest {
	private static DatabaseManagementService managementService;
	private static GraphDatabaseService database;
	
	public Neo4JUniversalTest() {
		super(new Neo4JTestDriver(getDatabase(), getDatabase().beginTx()));
	}
	
	@BeforeAll
	public static void initDb() {
		getDatabase(); // Initialize database
	}
	
	@AfterAll
	public static void closeDb() {
		if (managementService != null) {
			managementService.shutdown();
		}
	}
	
	public static GraphDatabaseService getDatabase() {
		if (database == null) {
			try {
				managementService = new DatabaseManagementServiceBuilder(Paths.get("target", "db"))
					.setConfig(GraphDatabaseSettings.preallocate_store_files, false)
					.setConfig(GraphDatabaseSettings.strict_config_validation, false)
					.build();
				database = managementService.database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
			} catch (Exception e) {
				throw new RuntimeException("Failed to initialize Neo4j database", e);
			}
		}
		return database;
	}
}
