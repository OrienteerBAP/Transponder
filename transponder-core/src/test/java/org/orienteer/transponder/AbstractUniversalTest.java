package org.orienteer.transponder;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.orienteer.transponder.Transponder.ITransponderHolder;
import org.orienteer.transponder.datamodel.ClassTestDAO;
import org.orienteer.transponder.datamodel.ISimpleEntity;
import org.orienteer.transponder.datamodel.ITestDAO;

import net.bytebuddy.dynamic.DynamicType.Builder;

public abstract class AbstractUniversalTest 
{
	public static final Random RANDOM = new Random();
	
	protected final ITestDriver driver;
	protected final Transponder transponder;
	
	public AbstractUniversalTest(ITestDriver driver) {
		this.driver = driver;
		this.transponder = new Transponder(driver);
	}
	
	@Test
	public void testInterfaceDAO() {
		ITestDAO dao = transponder.dao(ITestDAO.class);
		Integer checkInt = RANDOM.nextInt();
		assertEquals(checkInt, dao.echoDAO(checkInt));
	}
	
	@Test
	public void testClassDAO() {
		ClassTestDAO dao = transponder.dao(ClassTestDAO.class);
		Integer checkInt = RANDOM.nextInt();
		assertEquals(checkInt, dao.echoNumberDAO(checkInt));
	}
	
	@Test
	public void testJointDAO() {
		ClassTestDAO dao = transponder.dao(ClassTestDAO.class, ITestDAO.class);
		Integer checkInt = RANDOM.nextInt();
		assertEquals(checkInt, dao.echoNumberDAO(checkInt));
		assertEquals(checkInt, ((ITestDAO)dao).echoDAO(checkInt));
	}
	
	@Test
	public void testEntityDescription() {
		transponder.describe(ISimpleEntity.class);
		driver.assertHasType("Simple");
		driver.assertHasProperty("Simple", "name");
		driver.assertHasProperty("Simple", "description");
		driver.assertHasIndex("Simple", "nameDescription");
		driver.assertHasIndex("Simple", "nameValue");
		driver.assertHasIndex("Simple", "Simple.value");
		
		driver.assertHasType("Remote");
		driver.assertHasProperty("Remote", "remoteName");
		
		driver.assertHasType("Parametrized");
	}
	
	@Test
	public void testEntityCreation() {
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		String name = "Name"+RANDOM.nextInt();
		String description = "Description"+RANDOM.nextInt();
		entity.setName(name);
		entity.setDescription(description);
		assertEquals(name, entity.getName());
		assertEquals(description, entity.getDescription());
	}
	
	/*@Test
	public void testEntityProviding() {
		String name = "Name"+RANDOM.nextInt();
		String description = "Description"+RANDOM.nextInt();
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", name);
		map.put("description", description);
		ISimpleEntity entity = new Transponder(new TestDriver()).provide(map, ISimpleEntity.class);
		assertEquals(name, entity.getName());
		assertEquals(description, entity.getDescription());
	}*/
	
	@Test
	public void testTransponderPreserving() throws Exception {
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		assertTrue(entity instanceof ITransponderHolder);
		assertEquals(transponder, ((ITransponderHolder)entity).get$transponder());
		Field field = entity.getClass().getDeclaredField("$transponder");
		field.setAccessible(true);
		assertEquals(transponder, field.get(entity));
	}
	
	@Test
	public void testIgnoringDefaultGettersAndSetters() throws Exception {
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		assertEquals(ISimpleEntity.defaultValue, entity.getDefault());
		String echoDefaultValue = "Echo Default Value";
		assertEquals(echoDefaultValue, entity.setDefault(echoDefaultValue));
	}
	
	/*@Test
	public void testDAOQuery() {
		TestDriver driver = new TestDriver();
		driver.insertRecord("a", "name",   "Single A");
		driver.insertRecord("aa", "name",  "Two A");
		driver.insertRecord("aaa", "name", "Triple A");
		ITestDAO dao = new Transponder(driver).dao(ITestDAO.class);
		List<ISimpleEntity> all = dao.getAll();
		assertNotNull(all);
		assertEquals(3, all.size());
		
		ISimpleEntity ret = dao.getWithCharacter('b', 1);
		assertNull(ret);
		ret = dao.getWithCharacter('a', 1);
		assertNotNull(ret);
		assertEquals("Single A", ret.getName());
		
		ret = dao.getWithCharacter('a', 2);
		assertNotNull(ret);
		assertEquals("Two A", ret.getName());
		
		ret = dao.getWithCharacter('a', 3);
		assertNotNull(ret);
		assertEquals("Triple A", ret.getName());
	}
	
	@Test
	public void testLookupInDAO() {
		TestDriver driver = new TestDriver();
		driver.insertRecord("dao1", "name",  "DAO1");
		driver.insertRecord("dao2", "name",  "DAO2");
		ITestDAO dao = new Transponder(driver).dao(ITestDAO.class);
		ISimpleEntity entity = dao.lookupByPk("dao1");
		assertNotNull(entity);
		assertEquals("DAO1", entity.getName());
		entity = dao.lookupByPk("dao2");
		assertNotNull(entity);
		assertEquals("DAO2", entity.getName());
		entity = dao.lookupByPk("dao3");
		assertNull(entity);
		assertTrue(dao.checkPresenseByPk("dao1"));
		assertTrue(dao.checkPresenseByPk("dao2"));
		assertFalse(dao.checkPresenseByPk("dao3"));
	}
	
	@Test
	public void testLookupInEntity() {
		TestDriver driver = new TestDriver();
		driver.insertRecord("dao1", "name",  "DAO1");
		driver.insertRecord("dao2", "name",  "DAO2");
		ISimpleEntity entity = new Transponder(driver).create(ISimpleEntity.class);
		assertNotNull(entity.lookupByPk("dao1"));
		assertEquals("DAO1", entity.getName());
		assertNotNull(entity.lookupByPk("dao2"));
		assertEquals("DAO2", entity.getName());
		assertNull(entity.lookupByPk("dao3"));
		assertEquals("DAO2", entity.getName()); //Should stay the same
		
		assertTrue(entity.checkPresenseByPk("dao1"));
		assertEquals("DAO1", entity.getName());
		assertTrue(entity.checkPresenseByPk("dao2"));
		assertEquals("DAO2", entity.getName());
		assertFalse(entity.checkPresenseByPk("dao3"));
		assertEquals("DAO2", entity.getName()); //Should stay the same
	}
	
	@Test
	public void testCommand() {
		TestDriver driver = new TestDriver();
		driver.insertRecord("dao1", "name",  "DAO1");
		driver.insertRecord("dao2", "name",  "DAO2");
		ITestDAO dao = new Transponder(driver).dao(ITestDAO.class);
		ISimpleEntity entity = dao.removeByPk("dao1");
		assertNotNull(entity);
		assertEquals("DAO1", entity.getName());
		assertNull(dao.removeByPk("dao1"));
		assertNotNull(dao.removeByPk("dao2"));
		assertNull(dao.removeByPk("dao2"));
	}*/
	
	/*@Test
	public void testAutoWrapping() {
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		String name = "Other Name";
		String description = "Other Description";
		Map<String, Object> otherEntityMap = new HashMap<>();
		otherEntityMap.put("name", name);
		otherEntityMap.put("description", description);
		((Map<String, Object>)entity).put("otherEntity", otherEntityMap);
		ISimpleEntity otherEntity = entity.getOtherEntity();
		assertNotNull(otherEntity);
		assertEquals(name, otherEntity.getName());
		assertEquals(description, otherEntity.getDescription());
	}*/
	
	/*@Test
	public void testAutoUnwrapping() {
		TestDriver driver = new TestDriver();
		Transponder transponder = new Transponder(driver);
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		String name = "Other Name";
		String description = "Other Description";
		ISimpleEntity otherEntity = transponder.create(ISimpleEntity.class);
		otherEntity.setName(name);
		otherEntity.setDescription(description);
		entity.setOtherEntity(otherEntity);
		Map<String, Object> otherEntityMap = (Map<String, Object>)((Map<String, Object>)entity).get("otherEntity");
		assertNotNull(otherEntityMap);
		assertEquals(name, otherEntityMap.get("name"));
		assertEquals(description, otherEntityMap.get("description"));
	}*/
	
	@Test
	public void testChaining() {
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		ISimpleEntity otherEntity = entity.setName("Test Name");
		assertNotNull(otherEntity);
		assertEquals(entity, otherEntity);
	}
	
	@Test
	public void testDriverSpecificMutators() {
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		assertFalse(entity instanceof IMarker);
		
		entity = new Transponder(new TestDriver() {
			@Override
			public IMutator getMutator() {
				return new IMutator() {
					
					@Override
					public <T> Builder<T> mutate(Builder<T> builder, BuilderScheduler scheduler) {
						return builder.implement(IMarker.class);
					}
				};
			}
		}).create(ISimpleEntity.class);
		assertTrue(entity instanceof IMarker);
	}
	
	public static interface IMarker {
		
	}
	
	@Test
	public void testDefaultValue() {
		ITestDAO dao = transponder.dao(ITestDAO.class);
		assertEquals((Integer)10, dao.getDefaultValue(10));
		assertEquals((Integer)1, dao.getDefaultValue(null));
	
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		assertEquals("EMPTY", entity.getValue());
		entity.setValue("NOT EMPTY");
		assertEquals("NOT EMPTY", entity.getValue());
	}
	
	@Test
	public void testEntityProviding() {
		String name = "Name"+RANDOM.nextInt();
		String description = "Description"+RANDOM.nextInt();
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", name);
		map.put("description", description);
		Object seed = driver.createSeedObject("Simple", map);
		ISimpleEntity entity = transponder.provide(seed, ISimpleEntity.class);
		assertEquals(name, entity.getName());
		assertEquals(description, entity.getDescription());
	}
	
	@Test
	public void testAutoWrapping() {
		
		String name = "Other Name";
		String description = "Other Description";
		Object otherSeed = driver.createSeedObject("Simple", CommonUtils.toMap("pk", "other", "name", name, "description", description));
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		entity.setOtherEntity(transponder.provide(otherSeed, ISimpleEntity.class));
		ISimpleEntity otherEntity = entity.getOtherEntity();
		assertNotNull(otherEntity);
		assertEquals(name, otherEntity.getName());
		assertEquals(description, otherEntity.getDescription());
	}
	
	@Test
	public void testAutoUnwrapping() {
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		String name = "Other Name";
		String description = "Other Description";
		ISimpleEntity otherEntity = transponder.create(ISimpleEntity.class);
		otherEntity.setName(name);
		otherEntity.setDescription(description);
		entity.setOtherEntity(otherEntity);
		
		Object otherEntitySeed = driver.getPropertyValue(entity, "otherEntity");
		ISimpleEntity otherEntityInstance = transponder.provide(otherEntitySeed, ISimpleEntity.class);
		assertNotNull(otherEntityInstance);
		assertEquals(name, driver.getPropertyValue(otherEntityInstance, "name"));
		assertEquals(description, driver.getPropertyValue(otherEntityInstance, "description"));
	}
	
}