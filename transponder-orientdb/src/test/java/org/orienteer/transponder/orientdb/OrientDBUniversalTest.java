package org.orienteer.transponder.orientdb;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.orienteer.transponder.AbstractUniversalTest;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
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
	
	@BeforeClass
	public static void beforeDAOTest() {
		orientDB.createIfNotExists(DB_NAME, ODatabaseType.MEMORY);
	}
	
	@AfterClass
	public static void closeDb() {
		db.close();
		orientDB.close();
	}
	
	@Before
	public void makeSureThatDBInThecurrentThread() {
		getODatabaseSession().activateOnCurrentThread();
	}
	
	public OrientDBUniversalTest() {
		super(new OTestDriver());
	}
	
	public static ODatabaseSession getODatabaseSession() {
		if(db==null) {
			db = orientDB.open(DB_NAME,"admin","admin");
		}
		return db;
	}

}
