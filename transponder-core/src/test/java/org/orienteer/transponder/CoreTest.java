package org.orienteer.transponder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.orienteer.transponder.Transponder.ITransponderEntity;
import org.orienteer.transponder.datamodel.ClassTestDAO;
import org.orienteer.transponder.datamodel.ISimpleEntity;
import org.orienteer.transponder.datamodel.ITestDAO;

public class CoreTest 
{
	public static final Random RANDOM = new Random();
	
	@Test
	public void testInterfaceDAO() {
		ITestDAO dao = new Transponder(new TestDriver()).dao(ITestDAO.class);
		Integer checkInt = RANDOM.nextInt();
		assertEquals(checkInt, dao.echoDAO(checkInt));
	}
	
	@Test
	public void testClassDAO() {
		ClassTestDAO dao = new Transponder(new TestDriver()).dao(ClassTestDAO.class);
		Integer checkInt = RANDOM.nextInt();
		assertEquals(checkInt, dao.echoNumberDAO(checkInt));
	}
	
	@Test
	public void testJointDAO() {
		ClassTestDAO dao = new Transponder(new TestDriver()).dao(ClassTestDAO.class, ITestDAO.class);
		Integer checkInt = RANDOM.nextInt();
		assertEquals(checkInt, dao.echoNumberDAO(checkInt));
		assertEquals(checkInt, ((ITestDAO)dao).echoDAO(checkInt));
	}
	
	@Test
	public void testEntityDescription() {
		TestDriver testDriver = new TestDriver();
		new Transponder(testDriver).describe(ISimpleEntity.class);
		testDriver.assertHasType("Simple");
		testDriver.assertHasProperty("Simple", "name");
		testDriver.assertHasProperty("Simple", "description");
	}
	
	@Test
	public void testEntityCreation() {
		ISimpleEntity entity = new Transponder(new TestDriver()).create(ISimpleEntity.class);
		String name = "Name"+RANDOM.nextInt();
		String description = "Description"+RANDOM.nextInt();
		entity.setName(name);
		entity.setDescription(description);
		assertEquals(name, entity.getName());
		assertEquals(description, entity.getDescription());
	}
	
	@Test
	public void testEntityProviding() {
		String name = "Name"+RANDOM.nextInt();
		String description = "Description"+RANDOM.nextInt();
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", name);
		map.put("description", description);
		ISimpleEntity entity = new Transponder(new TestDriver()).provide(map, ISimpleEntity.class);
		assertEquals(name, entity.getName());
		assertEquals(description, entity.getDescription());
	}
	
	@Test
	public void testTransponderPreserving() throws Exception {
		Transponder transponder = new Transponder(new TestDriver());
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		assertTrue(entity instanceof ITransponderEntity);
		assertEquals(transponder, ((ITransponderEntity)entity).get$transponder());
		Field field = entity.getClass().getDeclaredField("$transponder");
		field.setAccessible(true);
		assertEquals(transponder, field.get(entity));
	}
	
	@Test
	public void testDAOQuery() {
		ITestDAO dao = new Transponder(new TestDriver()).dao(ITestDAO.class);
		assertEquals(ITestDAO.QUERY, dao.queryEcho());
	}
	
	
}
