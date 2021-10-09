package org.orienteer.transponder.orientdb;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.orienteer.transponder.Transponder;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import junit.framework.AssertionFailedError;

public class DAOTest {
	
	static final String DB_NAME = "TestDB";
	static final String TEST_CLASS = "DAOTestClass";
	
	private static OrientDB orientDB = new OrientDB("embedded:target/",OrientDBConfig.defaultConfig());
	private static ODatabaseSession db;
	
	private static Transponder transponder;

	@BeforeClass
	public static void beforeDAOTest() {
		orientDB.createIfNotExists(DB_NAME, ODatabaseType.MEMORY);
		
		ODatabaseSession db = getDatabase();
		
		OSchema schema = db.getMetadata().getSchema();
		OClass oClass = schema.getOrCreateClass(TEST_CLASS);
		oClass.createProperty("name", OType.STRING);
		oClass.createProperty("parent", OType.LINK);
		oClass.createProperty("child", OType.LINKLIST);
		oClass.createProperty("linkMap", OType.LINKMAP);
		
		ODocument root = new ODocument(TEST_CLASS);
		root.field("name", "root");
		root.save();
		Map<String, ODocument> linkMap = new HashMap<String, ODocument>();
		for(int i=0; i<5; i++) {
			String name = "Child#"+i;
			ODocument child = new ODocument(TEST_CLASS);
			child.field("name", name);
			child.field("parent", root);
			child.save();
			linkMap.put(name, child);
		}
		root.field("linkMap", linkMap);
		root.save();
		transponder = new Transponder(new ODriver());
	}
	
	@AfterClass
	public static void afterDAOTest() {
		
		getDatabase().getMetadata().getSchema().dropClass(TEST_CLASS);
	}
	
	@Before
	public void makeSureThatDBInThecurrentThread() {
		getDatabase().activateOnCurrentThread();
	}
	
	public static ODatabaseSession getDatabase() {
		if(db==null) {
			db = orientDB.open(DB_NAME,"admin","admin");
		}
		return db;
	}
	
	@Test
	public void testInjection() {
		IDAOTestClass doc = transponder.dao(IDAOTestClass.class);
		List<ODocument> perspectives = getDatabase().query(new OSQLSynchQuery<ODocument>("select from DAOTestClass"));
		for (ODocument oDocument : perspectives) {
			doc.fromStream(oDocument);
			assertEquals(oDocument.field("name"), doc.getName());
			assertEquals(oDocument.field("name"), doc.getNameSynonymMethod());
			assertEquals("test"+oDocument.field("name"), doc.getTestName());
			assertEquals("test2"+oDocument.field("name"), doc.getTest2Name());
			assertEquals("test3test"+oDocument.field("name"), doc.getTest3Name());
			assertEquals((Object)oDocument.field("name"), doc.getDocument().field("name"));
		}
	}
	
	@Test
	public void testLookups() {
		IDAOTestClass iOPerspective = transponder.dao(IDAOTestClass.class);
		assertTrue(iOPerspective.lookupToBoolean("root"));
		assertEquals("root", iOPerspective.getName());
		assertEquals("testroot", iOPerspective.getTestName());
		assertEquals("test2root", iOPerspective.getTest2Name());
		assertEquals("test3testroot", iOPerspective.getTest3Name());
		IDAOTestClass other = iOPerspective.lookupAsChain("root");
		assertSame(iOPerspective, other);
		assertNull(iOPerspective.lookupAsChain("notExistingPerspective"));
	}
	
	@Test
	public void testQuery() {
		IDAOTestClass iOPerspective = transponder.dao(IDAOTestClass.class);
		iOPerspective.lookupToBoolean("root");
		List<ODocument> menu = iOPerspective.listAllChild();
		assertNotNull(menu);
		assertTrue("Size of childs", menu.size()>0);
	}
	
	@Test
	public void testDAO() {
		ITestDAO dao = transponder.dao(ITestDAO.class);
		List<ODocument> testDocs = dao.listDAOTestClass();
		assertNotNull(testDocs);
		assertTrue("Size of test docs", testDocs.size()>0);
		assertTrue("Size of test docs", dao.countAll()>0);
		assertEquals(testDocs.size(), dao.countAll());
	}
	
	@Test
	public void testMirroring() {
		IDAOTestClass doc = transponder.dao(IDAOTestClass.class);
		doc.lookupToBoolean("root");
		assertNotNull(doc.getDocument());
		Object reloadRet = doc.reload();
		assertTrue(reloadRet == doc);
	}
	
	/*@Test
	public void testInterceptors() {
		IDAOTestClass doc = tester.getApplication().getServiceInstance(IDAOTestClass.class);
		assertEquals(TestDAOMethodHandler.RETURN, doc.interceptedInvocation());
	}*/
	
	@Test
	public void testParentChildDefaultMethods() {
		IDAOChild obj = transponder.create(IDAOChild.class);
		assertEquals(-1, obj.methodWithNoBodyInParent());
		assertEquals(IDAOChild.class, obj.methodWithDefaultBodyInParent());
		obj.methodVoidWithException();
		
		try {
			obj.methodVoid();
			throw new AssertionFailedError("We shouldn't be there");
		} catch (IllegalStateException e) {
			//It's expected
		}
	}
	
	@Test
//	@Sudo
	public void testConvertions() {
		ITestDAO dao = transponder.dao(ITestDAO.class);
		ODocument doc = dao.findSingleAsDocument("root");
		IDAOTestClass root = dao.findSingleAsDAO("root");
		assertEquals(doc.field("name"), root.getName());
		root.setPrimitiveSupported(true);
		assertEquals(true, root.isPrimitiveSupported()); 
		assertEquals(-100, (int)root.returnDefaultValue());
		
		List<ODocument> listDocs = dao.findAllAsDocument();
		List<IDAOTestClass> listObjs = dao.findAllAsDAO();
		assertEquals(listDocs.size(), listObjs.size());
		assertTrue(listDocs.get(0) instanceof ODocument);
		assertTrue(listObjs.get(0) instanceof IDAOTestClass);
		
		assertConsistent(root.getChild(), root.getChildAsDocuments(), 5);
		
		List<ODocument> allExceptOneChild = root.getChildAsDocuments();
		allExceptOneChild.remove(0);
		root.setChildAsDocuments(allExceptOneChild);
		root.save();
		
		assertConsistent(root.getChild(), root.getChildAsDocuments(), 4);
		
		List<IDAOTestClass> allExceptOneMoreChild = new ArrayList<IDAOTestClass>(root.getChild());
		allExceptOneMoreChild.remove(0);
		root.setChild(allExceptOneMoreChild);
		root.save();
		
		assertConsistent(root.getChild(), root.getChildAsDocuments(), 3);
		
		assertConsistent(root.getLinkMap(), root.getLinkMapAsDocuments(), 5);
		
		Map<String, ODocument> mapDocs = root.getLinkMapAsDocuments();
//		Map<String, ODocument> mapDocs = new HashMap<String, ODocument>(root.getLinkMapAsDocuments());
		Iterator<Map.Entry<String, ODocument>> itDocs = mapDocs.entrySet().iterator();
		itDocs.next();
		itDocs.remove();
		root.setLinkMapAsDocuments(mapDocs);
		root.save();
		
		assertConsistent(root.getLinkMap(), root.getLinkMapAsDocuments(), 4);
		
		
		Map<String, IDAOTestClass> map = new HashMap<String, IDAOTestClass>(root.getLinkMap());
		Iterator<Map.Entry<String, IDAOTestClass>> it = map.entrySet().iterator();
		it.next();
		it.remove();
		root.setLinkMap(map);
		root.save();
		
		assertConsistent(root.getLinkMap(), root.getLinkMapAsDocuments(), 3);
		
	}
	
	private void assertConsistent(List<IDAOTestClass> child, List<ODocument> childAsDoc, int size) {
		assertEquals(child.size(), childAsDoc.size());
		assertThat(child, hasSize(size));
		assertThat(childAsDoc, hasSize(size));
		
		assertThat(child, everyItem(isA(IDAOTestClass.class)));
		assertThat(childAsDoc, everyItem(isA(ODocument.class)));
	}
	
	private void assertConsistent(Map<String, IDAOTestClass> map, Map<String, ODocument> mapOfDocs, int size) {
		assertThat(map, aMapWithSize(size));
		assertThat(map.keySet(), everyItem(isA(String.class)));
		assertThat(map.values(), everyItem(isA(IDAOTestClass.class)));
		
		assertThat(mapOfDocs, aMapWithSize(size));
		assertThat(mapOfDocs.keySet(), everyItem(isA(String.class)));
		assertThat(mapOfDocs.values(), everyItem(isA(ODocument.class)));
	}
	
	@Test
//	@Sudo
	public void testDescriber() {
		OSchema schema = getDatabase().getMetadata().getSchema();
		try {
			transponder.describe(IDAOTestClassA.class);
			assertTrue(schema.existsClass("DAOTestClassRoot"));
			assertTrue(schema.existsClass("DAOTestClassA"));
			assertTrue(schema.existsClass("DAOTestClassB"));
			OClass daoTestClassRoot = schema.getClass("DAOTestClassRoot");
			OClass daoTestClassA = schema.getClass("DAOTestClassA");
			OClass daoTestClassB = schema.getClass("DAOTestClassB");
			OClass daoTestParametrized = schema.getClass("DAOTestParametrized");
			
			assertEquals(IDAOTestClassRoot.class.getName(), daoTestClassRoot.getCustom(ODriver.OCLASS_CUSTOM_TRANSPONDER_WRAPPER));
			assertEquals(IDAOTestClassA.class.getName(), daoTestClassA.getCustom(ODriver.OCLASS_CUSTOM_TRANSPONDER_WRAPPER));
			assertEquals(IDAOTestClassB.class.getName(), daoTestClassB.getCustom(ODriver.OCLASS_CUSTOM_TRANSPONDER_WRAPPER));
			
			assertTrue(daoTestClassRoot.isAbstract());
			assertProperty(daoTestClassRoot, "root", OType.STRING);
			assertNotNull(daoTestClassRoot.getClassIndex("DAOTestClassRoot.root"));
			
			OProperty root = assertProperty(daoTestClassA, "root", OType.STRING);
			assertEquals("DAOTestClassRoot.root", root.getFullName());
			
			assertProperty(daoTestClassA, "name", OType.STRING);
			assertProperty(daoTestClassA, "bSingle", OType.LINK, daoTestClassB);
			assertProperty(daoTestClassA, "bOtherField", OType.LINK, daoTestClassB);
			assertProperty(daoTestClassA, "selfType", OType.LINK, daoTestClassA);
			assertProperty(daoTestClassA, "linkAsDoc", OType.LINK, daoTestClassB);
			assertProperty(daoTestClassA, "embeddedStringList", OType.EMBEDDEDLIST, OType.STRING);
			assertProperty(daoTestClassA, "linkList", OType.LINKLIST, daoTestClassB);
			assertNotNull(daoTestClassA.getClassIndex("rootname"));
			
			assertProperty(daoTestClassB, "alias", OType.STRING);
			assertProperty(daoTestClassB, "linkToA", OType.LINK, daoTestClassA);
			assertProperty(daoTestClassB, "parameterizedLink", OType.LINK, daoTestParametrized);
		} finally {
			if(schema.existsClass("DAOTestClassA")) schema.dropClass("DAOTestClassA");
			if(schema.existsClass("DAOTestClassB")) schema.dropClass("DAOTestClassB");
			if(schema.existsClass("DAOTestClassRoot")) schema.dropClass("DAOTestClassRoot");
		}
	}
	
	/*@Test
	public void testProperMethodListOrder() throws Exception {
		Class<?> type = IDAOTestClassA.class;
		
		List<Method> methods = DAO.listMethods(type);
		assertEquals("getName", methods.get(0).getName());
		assertEquals("setName", methods.get(1).getName());
	}*/
	
	@Test
//	@Sudo
	public void testDescribeAllTypes() {
		OSchema schema = getDatabase().getMetadata().getSchema();
		try {
			transponder.describe(IDAOAllTypesTestClass.class);
			assertTrue(schema.existsClass("DAOAllTypesTestClass"));
			assertTrue(schema.existsClass("IDAODummyClass"));
			OClass oClass = schema.getClass("DAOAllTypesTestClass");
			OClass dummyClass = schema.getClass("IDAODummyClass");
			
			assertTrue(!oClass.isAbstract());
			
			assertProperty(oClass, "boolean", OType.BOOLEAN, false);
			assertProperty(oClass, "booleanPrimitive", OType.BOOLEAN, true);
			assertProperty(oClass, "booleanDeclared", OType.BOOLEAN, true);
			
			assertProperty(oClass, "integer", OType.INTEGER);
			assertProperty(oClass, "short", OType.SHORT);
			assertProperty(oClass, "long", OType.LONG);
			assertProperty(oClass, "float", OType.FLOAT);
			assertProperty(oClass, "double", OType.DOUBLE);
			assertProperty(oClass, "dateTime", OType.DATETIME);
			assertProperty(oClass, "date", OType.DATE);
			assertProperty(oClass, "string", OType.STRING);
			assertProperty(oClass, "binary", OType.BINARY); //
			assertProperty(oClass, "decimal", OType.DECIMAL);
			assertProperty(oClass, "byte", OType.BYTE);
			assertProperty(oClass, "custom", OType.CUSTOM);
			assertProperty(oClass, "transient", OType.TRANSIENT);
			assertProperty(oClass, "any", OType.ANY);
			
			assertProperty(oClass, "link", OType.LINK, dummyClass);
			assertProperty(oClass, "linkList", OType.LINKLIST, dummyClass);
			assertProperty(oClass, "linkSet", OType.LINKSET, dummyClass);
			assertProperty(oClass, "linkMap", OType.LINKMAP, dummyClass);
			assertProperty(oClass, "linkBag", OType.LINKBAG, dummyClass);
			
			assertProperty(oClass, "embedded", OType.EMBEDDED, dummyClass);
			assertProperty(oClass, "embeddedList", OType.EMBEDDEDLIST, dummyClass);
			assertProperty(oClass, "embeddedSet", OType.EMBEDDEDSET, dummyClass);
			assertProperty(oClass, "embeddedMap", OType.EMBEDDEDMAP, dummyClass);
			
			assertProperty(oClass, "embeddedStringSet", OType.EMBEDDEDSET, OType.STRING);
			assertProperty(oClass, "embeddedStringList", OType.EMBEDDEDLIST, OType.STRING);
			assertProperty(oClass, "embeddedStringMap", OType.EMBEDDEDMAP, OType.STRING);
			
			assertProperty(oClass, "enum", OType.STRING);

			assertProperty(oClass, "docs", OType.LINKLIST, dummyClass);

		} finally {
			if(schema.existsClass("DAOAllTypesTestClass")) schema.dropClass("DAOAllTypesTestClass");
			if(schema.existsClass("IDAODummyClass")) schema.dropClass("IDAODummyClass");
		}
	}
	
	private OProperty assertProperty(OClass oClass, String property, OType oType, OClass linkedClass) {
		OProperty prop = assertProperty(oClass, property, oType);
		assertEquals(linkedClass, prop.getLinkedClass());
		return prop;
	}
	
	private OProperty assertProperty(OClass oClass, String property, OType oType, OType linkedType) {
		OProperty prop = assertProperty(oClass, property, oType);
		assertEquals(linkedType, prop.getLinkedType());
		return prop;
	}
	
	private OProperty assertProperty(OClass oClass, String property, OType oType, boolean notNull) {
		OProperty prop = assertProperty(oClass, property, oType);
		assertEquals(notNull, prop.isNotNull());
		return prop;
	}
	
	private OProperty assertProperty(OClass oClass, String property, OType oType) {
		OProperty prop = oClass.getProperty(property);
		assertNotNull("Property '"+property+"'was not found on OClass:"+oClass, prop);
		assertEquals(oType, prop.getType());
		return prop;
	}
	
	@Test
//	@Sudo
	public void testInheritedClass() {
		OSchema schema = getDatabase().getMetadata().getSchema();
		try {
			transponder.describe(IDAOTestClassA.class);
			IDAOTestClassA obj = transponder.create(IDAOTestClassA.class);
			obj.setName("TestInheritedClass");
			ODriver.save(obj);
			IDAOTestClassRoot obj2 = transponder.provide(ODriver.asDocument(obj), IDAOTestClassRoot.class);
			assertTrue(obj2 instanceof IDAOTestClassA);
			assertEquals(obj.hashCode(), obj2.hashCode());
			assertTrue(obj2.equals(obj));
			
			IDAOTestClassB obj3 = transponder.create(IDAOTestClassB.class);
			ODriver.save(obj3);
			assertFalse(obj.equals(obj3));
		} finally {
			if(schema.existsClass("DAOTestClassA")) schema.dropClass("DAOTestClassA");
			if(schema.existsClass("DAOTestClassB")) schema.dropClass("DAOTestClassB");
			if(schema.existsClass("DAOTestClassRoot")) schema.dropClass("DAOTestClassRoot");
		}
	}
	
}
