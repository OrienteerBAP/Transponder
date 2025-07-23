package org.orienteer.transponder.orientdb;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
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
	private static OrientDB orientDB = new OrientDB("embedded:target/",OrientDBConfig.builder()
																			.addConfig(OGlobalConfiguration.CREATE_DEFAULT_USERS, true)
																			.build());
	private static ODatabaseSession db;
	
	@BeforeAll
	public static void beforeDAOTest() {
		orientDB.createIfNotExists(DB_NAME, ODatabaseType.MEMORY);
		db = orientDB.open(DB_NAME,"admin","admin");
	}
	
	@AfterAll
	public static void closeDb() {
		db.close();
		orientDB.close();
	}
	
	@BeforeEach
	public void makeSureThatDBInThecurrentThread() {
		ODatabaseSession session = getODatabaseSession();
		session.activateOnCurrentThread();
		ODatabaseRecordThreadLocal.instance().set((ODatabaseDocumentInternal) session);
	}
	
	public OrientDBUniversalTest() {
		super(new OTestDriver());
	}
	
	public static ODatabaseSession getODatabaseSession() {
		if(db==null) {
			db = orientDB.open(DB_NAME,"admin","admin");
		}
		// Ensure the database is always set in thread-local context
		db.activateOnCurrentThread();
		ODatabaseRecordThreadLocal.instance().set((ODatabaseDocumentInternal) db);
		return db;
	}

}
