package org.orienteer.transponder.arcadedb;

import org.junit.BeforeClass;
import org.orienteer.transponder.AbstractUniversalTest;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;

public class ArcadeDBUniversalTest extends AbstractUniversalTest {
	private static DatabaseFactory databaseFactory = new DatabaseFactory("target/db");
	private static Database database;
	
	public ArcadeDBUniversalTest() {
		super(new ArcadeDBTestDriver(database));
	}
	
	@BeforeClass
	public static void initDb() {
		database = databaseFactory.exists() ? databaseFactory.open() : databaseFactory.create();
	}
	
	public static void closeDb() {
		if(database!=null) database.close();
	}
}
