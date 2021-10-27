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
import org.orienteer.transponder.datamodel.sample.IFile;
import org.orienteer.transponder.datamodel.sample.IFileSystem;
import org.orienteer.transponder.datamodel.sample.IFolder;

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
		transponder.define(ISimpleEntity.class);
		driver.assertHasType("Simple");
		driver.assertHasProperty("Simple", "name");
		driver.assertHasProperty("Simple", "description");
		driver.assertHasReferenceProperty("Simple", "remoteEntity", "Remote");
		driver.assertHasReferenceProperty("Simple", "parametrizedEntity", "Parametrized");
		driver.assertHasIndex("Simple", "nameDescription", "name", "description");
		driver.assertHasIndex("Simple", "nameValue", "name", "value");
		driver.assertHasIndex("Simple", "Simple.value", "value");
		
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
	
	@Test
	public void testTransponderPreserving() throws Exception {
		transponder.define(ISimpleEntity.class);
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
					public <T> Builder<T> mutate(Transponder transponder, Builder<T> builder, BuilderScheduler scheduler) {
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
	
	@Test
	public void testDAOQuery() {
		ITestDAO dao = transponder.dao(ITestDAO.class);
		
		dao.deleteAll();
		List<ISimpleEntity> all = dao.getAll();
		assertNotNull(all);
		assertEquals(0, all.size());
		
		driver.createSeedObject("Simple", CommonUtils.toMap("pk", "a", "name", "Single A"));
		driver.createSeedObject("Simple", CommonUtils.toMap("pk", "aa", "name",  "Two A"));
		driver.createSeedObject("Simple", CommonUtils.toMap("pk", "aaa", "name", "Triple A"));
		all = dao.getAll();
		assertNotNull(all);
		assertEquals(3, all.size());
		
		ISimpleEntity ret = dao.lookupByPk("b");
		assertNull(ret);
		ret = dao.lookupByPk("a");
		assertNotNull(ret);
		assertEquals("Single A", ret.getName());
		
		ret = dao.lookupByPk("aa");
		assertNotNull(ret);
		assertEquals("Two A", ret.getName());
		
		ret = dao.lookupByPk("aaa");
		assertNotNull(ret);
		assertEquals("Triple A", ret.getName());
	}
	
	@Test
	public void testLookupInDAO() {
		driver.createSeedObject("Simple", CommonUtils.toMap("pk", "dao1LookupInDAO", "name",  "DAO1"));
		driver.createSeedObject("Simple", CommonUtils.toMap("pk", "dao2LookupInDAO", "name",  "DAO2"));
		ITestDAO dao = transponder.dao(ITestDAO.class);
		ISimpleEntity entity = dao.lookupByPk("dao1LookupInDAO");
		assertNotNull(entity);
		assertEquals("DAO1", entity.getName());
		entity = dao.lookupByPk("dao2LookupInDAO");
		assertNotNull(entity);
		assertEquals("DAO2", entity.getName());
		entity = dao.lookupByPk("dao3LookupInDAO");
		assertNull(entity);
		assertTrue(dao.checkPresenseByPk("dao1LookupInDAO"));
		assertTrue(dao.checkPresenseByPk("dao2LookupInDAO"));
		assertFalse(dao.checkPresenseByPk("dao3LookupInDAO"));
	}
	
	@Test
	public void testLookupInEntity() {
		driver.createSeedObject("Simple", CommonUtils.toMap("pk", "dao1LookupInEntity", "name",  "DAO1"));
		driver.createSeedObject("Simple", CommonUtils.toMap("pk", "dao2LookupInEntity", "name",  "DAO2"));
		ISimpleEntity entity = transponder.create(ISimpleEntity.class);
		assertNotNull(entity.lookupByPk("dao1LookupInEntity"));
		assertEquals("DAO1", entity.getName());
		assertNotNull(entity.lookupByPk("dao2LookupInEntity"));
		assertEquals("DAO2", entity.getName());
		assertNull(entity.lookupByPk("dao3LookupInEntity"));
		assertEquals("DAO2", entity.getName()); //Should stay the same
		
		assertTrue(entity.checkPresenseByPk("dao1LookupInEntity"));
		assertEquals("DAO1", entity.getName());
		assertTrue(entity.checkPresenseByPk("dao2LookupInEntity"));
		assertEquals("DAO2", entity.getName());
		assertFalse(entity.checkPresenseByPk("dao3LookupInEntity"));
		assertEquals("DAO2", entity.getName()); //Should stay the same
	}
	
	@Test
	public void testCommand() {
		ITestDAO dao = transponder.dao(ITestDAO.class);
		int zeroSize = dao.getAll().size();
		driver.createSeedObject("Simple", CommonUtils.toMap("pk", "dao1Command", "name",  "DAO1"));
		driver.createSeedObject("Simple", CommonUtils.toMap("pk", "dao2Command", "name",  "DAO2"));
		assertEquals(zeroSize+2, dao.getAll().size());
		dao.removeByPk("dao1Command");
		assertEquals(zeroSize+1, dao.getAll().size());
		dao.removeByPk("dao1Command");
		assertEquals(zeroSize+1, dao.getAll().size());
		dao.removeByPk("dao2Command");
		assertEquals(zeroSize, dao.getAll().size());
	}
	
	@Test
	public void testSampleDataModel() {
		transponder.define(IFile.class, IFolder.class);
		driver.assertHasType("File");
		driver.assertHasType("Folder");
		driver.assertHasType("Entry");
		driver.assertHasProperty("Folder", "name");
		driver.assertHasReferenceProperty("Entry", "parent", "Folder");
		driver.assertHasReferenceProperty("Folder", "child", "Entry");
		
		IFolder rootFolder = transponder.create(IFolder.class);
		rootFolder.setName("Root");
		Transponder.save(rootFolder);
		
		IFileSystem fileSystem = transponder.dao(IFileSystem.class);
		IFolder folder = fileSystem.getRoot("Root");
		assertEquals("Root", folder.getName());
	}
	
}
