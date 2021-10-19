package org.orienteer.transponder;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.orienteer.transponder.IPolyglot.Translation;
import org.orienteer.transponder.Transponder.ITransponderHolder;
import org.orienteer.transponder.datamodel.ClassTestDAO;
import org.orienteer.transponder.datamodel.ISimpleEntity;
import org.orienteer.transponder.datamodel.ITestDAO;

import net.bytebuddy.dynamic.DynamicType.Builder;

public class CoreSpecificTest 
{
	public static final Random RANDOM = new Random();
	
	@Test
	public void testDAOQuery() {
		TestDriver driver = new TestDriver();
		driver.insertRecord("a", "name",   "Single A");
		driver.insertRecord("aa", "name",  "Two A");
		driver.insertRecord("aaa", "name", "Triple A");
		ITestDAO dao = new Transponder(driver).dao(ITestDAO.class);
		List<ISimpleEntity> all = dao.getAll();
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
	}
	
	@Test
	public void testPolyglotLoading() {
		Transponder transponder = new Transponder(new TestDriver());
		IPolyglot polyglot = transponder.getPolyglot();
		Translation translation = polyglot.translate(CoreSpecificTest.class, "loaded1", "", "", "test", "test");
		assertEquals("text", translation.getLanguage());
		assertEquals("This was loaded", translation.getQuery());
		translation = polyglot.translate(CoreSpecificTest.class, "loaded2", "text", "Original Query", "test", "test");
		assertEquals("text", translation.getLanguage());
		assertEquals("Keep source lang", translation.getQuery());
	}
	
}
