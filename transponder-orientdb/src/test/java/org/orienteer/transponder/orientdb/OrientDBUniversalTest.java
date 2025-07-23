package org.orienteer.transponder.orientdb;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.orienteer.transponder.AbstractUniversalTest;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;

public class OrientDBUniversalTest extends AbstractUniversalTest {
	
	private static final String DB_NAME = "UniversalTestDB";
	private static OrientDB orientDB;
	private static ODatabaseSession db;
	
	public OrientDBUniversalTest() {
		super(new OTestDriver());
	}
	
	@BeforeAll
	public static void initDb() {
		getODatabaseSession(); // Initialize database
	}
	
	@AfterAll
	public static void closeDb() {
		if (db != null && !db.isClosed()) {
			db.close();
		}
		if (orientDB != null) {
			orientDB.close();
		}
	}
	
	public static ODatabaseSession getODatabaseSession() {
		if (orientDB == null) {
			// Initialize OrientDB with proper Java 11 compatible configuration
			orientDB = new OrientDB("embedded:target/", OrientDBConfig.builder()
					.addConfig(OGlobalConfiguration.CREATE_DEFAULT_USERS, true)
					.addConfig(OGlobalConfiguration.DB_POOL_MIN, 1)
					.addConfig(OGlobalConfiguration.DB_POOL_MAX, 1)
					.build());
			
			// Ensure database exists and is properly initialized
			if (!orientDB.exists(DB_NAME)) {
				orientDB.create(DB_NAME, ODatabaseType.MEMORY);
			}
		}
		
		// Ensure database session is available and active
		if (db == null || db.isClosed()) {
			db = orientDB.open(DB_NAME, "admin", "admin");
		}
		
		// Always ensure proper thread-local context
		try {
			db.activateOnCurrentThread();
			ODatabaseRecordThreadLocal.instance().set((ODatabaseDocumentInternal) db);
		} catch (Exception e) {
			// If activation fails, try reopening the session
			db = orientDB.open(DB_NAME, "admin", "admin");
			db.activateOnCurrentThread();
			ODatabaseRecordThreadLocal.instance().set((ODatabaseDocumentInternal) db);
		}
		
		return db;
	}
}