package org.orienteer.transponder.arcadedb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.orienteer.transponder.AbstractUniversalTest;
import org.orienteer.transponder.CommonUtils;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.arcadedb.database.Document;
import com.arcadedb.database.MutableDocument;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema.INDEX_TYPE;
import com.arcadedb.schema.Type;

public class ArcadeDBUniversalTest extends AbstractUniversalTest {
	private static DatabaseFactory databaseFactory = new DatabaseFactory("target/db");
	private static Database database;
	
	public ArcadeDBUniversalTest() {
		super(new ArcadeDBTestDriver(database));
	}
	
	@BeforeClass
	public static void initDb() {
		database = databaseFactory.exists() ? databaseFactory.open() : databaseFactory.create();
		database.setAutoTransaction(true);
	}
	
	public static void closeDb() {
		if(database!=null) database.close();
	}
	
	@Test
	@Ignore
	public void testDocumentAfterCreation() {
		DocumentType typeRoot = database.getSchema().getOrCreateDocumentType("TestRoot");
		typeRoot.getOrCreateProperty("name", String.class);
		typeRoot.getOrCreateTypeIndex(INDEX_TYPE.FULL_TEXT, true, "name");
		database.command("sql", "delete from TestRoot");
		
		DocumentType typeChild = database.getSchema().getOrCreateDocumentType("TestChild");
		typeChild.setParentTypes(Arrays.asList(typeRoot));
		MutableDocument doc =  database.newDocument("TestChild");
		doc.set("name", "Document Name");
		assertEquals("Document Name", doc.get("name"));
		doc.save();
		assertEquals("Document Name", doc.get("name"));
		try(ResultSet rs = database.query("sql", "select from TestChild where name = :name", CommonUtils.toMap("arg0", "Test2", "name", "Document Name"))) {
			assertTrue(rs.hasNext());
			Document docRetrieved = rs.next().getElement().orElse(null);
			assertEquals("Document Name", docRetrieved.get("name"));
			assertFalse(rs.hasNext());
		}
	}
	
	@Test
	@Ignore
	public void testDocumentAfterCreation2() {
		DocumentType typeRoot = database.getSchema().getOrCreateDocumentType("TestRoot2");
		typeRoot.getOrCreateProperty("name", String.class);
		typeRoot.getOrCreateProperty("parent", Type.LINK);
		typeRoot.getOrCreateTypeIndex(INDEX_TYPE.LSM_TREE, true, "name", "parent");
		database.command("sql", "delete from TestRoot2");
		
		DocumentType typeChild = database.getSchema().getOrCreateDocumentType("TestChild2");
		typeChild.setParentTypes(Arrays.asList(typeRoot));
		MutableDocument doc =  database.newDocument("TestChild2");
		doc.set("name", "Document Name");
		assertEquals("Document Name", doc.get("name"));
		doc.save();
		assertEquals("Document Name", doc.get("name"));
		try(ResultSet rs = database.query("sql", "select from TestChild2 where name = :name", CommonUtils.toMap("arg0", "Test2", "name", "Document Name"))) {
			assertTrue(rs.hasNext());
			Document docRetrieved = rs.next().getElement().orElse(null);
			assertEquals("Document Name", docRetrieved.get("name"));
			assertFalse(rs.hasNext());
		}
	}
}
