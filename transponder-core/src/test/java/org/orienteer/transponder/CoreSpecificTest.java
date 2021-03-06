package org.orienteer.transponder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.orienteer.transponder.IPolyglot.Translation;
import org.orienteer.transponder.Transponder.ITransponderDelegator;
import org.orienteer.transponder.annotation.AdviceAnnotation;
import org.orienteer.transponder.annotation.DefaultValue;
import org.orienteer.transponder.annotation.DelegateAnnotation;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.annotation.Lookup;
import org.orienteer.transponder.annotation.OverrideByThis;
import org.orienteer.transponder.datamodel.ClassTestDAO;
import org.orienteer.transponder.datamodel.IRemoteEntity;
import org.orienteer.transponder.datamodel.ISimpleEntity;
import org.orienteer.transponder.datamodel.ITestDAO;

import net.bytebuddy.asm.Advice;

public class CoreSpecificTest 
{
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
	
	@Test
	public void testDriverPolymorphic() {
		TestDriver driver = new TestDriver();
		driver.createType("Root", false, null);
		driver.createProperty("Root", "name", String.class, null, 0, null);
		driver.assertHasType("Root");
		driver.assertHasProperty("Root", "name");
		driver.createType("Child", false, null, "Root");
		driver.assertHasType("Root");
		driver.assertHasProperty("Child", "name");
	}
	
	@Test
	public void testDynamicAdvicesAndImplementations() {
		Transponder transponder = new Transponder(new TestDriver());
		DynamicDAO dao = transponder.dao(DynamicDAO.class);
		assertEquals(DynamicDAO.STATIC_VALUE, dao.getFromImplementation());
		assertEquals((Integer)1, dao.getIncOnce());
		assertEquals((Integer)(DynamicDAO.STATIC_VALUE+1), dao.getIncOnStatic());
		assertEquals((Integer)(DynamicDAO.STATIC_VALUE+1+2), dao.getIncOnStaticTwice());
		assertEquals((Integer)3, dao.getIncOnDefaultTwice());
	}
	
	public static interface DynamicDAO {
		
		public static final Integer STATIC_VALUE = (int)Short.MAX_VALUE;
		
		@DelegateAnnotation(Delegate.class)
		public Integer getFromImplementation();
		
		@AdviceAnnotation(IncAdvice.class)
		public default Integer getIncOnce() {
			return 0;
		}
		
		@DelegateAnnotation(Delegate.class)
		@AdviceAnnotation(IncAdvice.class)
		public Integer getIncOnStatic();
		
		@DelegateAnnotation(Delegate.class)
		@AdviceAnnotation(IncAdvice.class)
		@AdviceAnnotation(Inc2Advice.class)
		public Integer getIncOnStaticTwice();
		
		@AdviceAnnotation(IncAdvice.class)
		@AdviceAnnotation(Inc2Advice.class)
		public default Integer getIncOnDefaultTwice() {
			return 0;
		}
		
	}
	
	public static class Delegate {
		public static Integer getValue() {
			return DynamicDAO.STATIC_VALUE;
		}
	}
	
	public static class IncAdvice {
		
		@Advice.OnMethodExit
		public static void incValue(@Advice.Return(readOnly = false) Integer ret) {
			ret = ret + 1;
		}
	}
	
	public static class Inc2Advice {
		
		@Advice.OnMethodExit
		public static void incValue(@Advice.Return(readOnly = false) Integer ret) {
			ret = ret + 2;
		}
	}
	
	@Test
	public void testProperOrder() {
		TestDriver driver = new TestDriver();
		Transponder transponder = new Transponder(driver);
		transponder.define(ISimpleEntity.class);
		assertTrue(driver.hasPropertyWithOrder("Simple", "pk", 100));
		assertTrue(driver.hasPropertyWithOrder("Simple", "name", 105));
		assertTrue(driver.hasPropertyWithOrder("Simple", "description", 110));
		assertTrue(driver.hasPropertyWithOrder("Simple", "value", 500));
		assertTrue(driver.hasPropertyWithOrder("Simple", "otherEntity", 115));
		assertTrue(driver.hasPropertyWithOrder("Simple", "remoteEntity", 120));
		assertTrue(driver.hasPropertyWithOrder("Simple", "parametrizedEntity", 125));
		
		transponder.define(IRemoteEntity.class);
		assertTrue(driver.hasPropertyWithOrder("Remote", "remoteName", 0));
	}
	
	@Test
	public void testDefaultValue() {
		Transponder transponder = new Transponder(new TestDriver());
		DefaultValueTestCaseEntity entity = transponder.create(DefaultValueTestCaseEntity.class);
		assertEquals(null, entity.getNoDefaultObject());
		assertEquals(0, entity.getNoDefaultPrimitive());
		assertEquals((Integer)100, entity.getDefaultValueObject());
		assertEquals(300, entity.getDefaultValuePrimitive());
	}
	
	@EntityType("DefaultValueTestCaseEntity")
	public static interface DefaultValueTestCaseEntity {
		
		public Integer getNoDefaultObject();
		
		public int getNoDefaultPrimitive();
		
		@DefaultValue("100")
		public Integer getDefaultValueObject();
		
		@DefaultValue("300")
		public int getDefaultValuePrimitive();
	}
	
	@Test
	public void testProperOrderForInvert() {
		//Test creation from top to bottom
		TestDriver driver = new TestDriver();
		Transponder transponder = new Transponder(driver);
		transponder.define(IFolder.class);
		assertCreatedModel(driver);
		//Test creation from bottom to top
		driver = new TestDriver();
		transponder = new Transponder(driver);
		transponder.define(IFile.class);
		assertCreatedModel(driver);
		//Test joint creation in one order
		driver = new TestDriver();
		transponder = new Transponder(driver);
		transponder.define(IFolder.class, IFile.class);
		assertCreatedModel(driver);
		//Test joint creation in reverse order
		driver = new TestDriver();
		transponder = new Transponder(driver);
		transponder.define(IFile.class, IFolder.class);
		assertCreatedModel(driver);
	}
	
	private void assertCreatedModel(TestDriver driver) {
		driver.assertHasType("Folder");
		driver.assertHasType("File");
		driver.assertHasProperty("Folder", "name");
		driver.assertHasProperty("Folder", "files");
		driver.assertHasProperty("Folder", "subFolders");
		driver.assertHasProperty("Folder", "folder");
		driver.assertHasProperty("File", "name");
		driver.assertHasProperty("File", "folder");
		assertEquals("File.folder", driver.getPolymorphicProperty("Folder", "files").getInverse());
		assertEquals("Folder.files", driver.getPolymorphicProperty("File", "folder").getInverse());
		assertEquals("Folder.folder", driver.getPolymorphicProperty("Folder", "subFolders").getInverse());
		assertEquals("Folder.subFolders", driver.getPolymorphicProperty("Folder", "folder").getInverse());
	}
	
	@EntityType("Folder")
	public static interface IFolder {
		public String getName();
		@EntityProperty(inverse = "folder")
		public List<IFile> getFiles();
		
		@EntityProperty(inverse = "folder")
		public List<IFolder> getSubFolders();
		
		@EntityProperty(inverse = "subFolders")
		public IFolder getFolder();
	}
	
	@EntityType("File")
	public static interface IFile {
		public String getName();
		@EntityProperty(inverse = "files")
		public IFolder getFolder();
	}
	
	@Test
	public void testNamingStrategy() {
		Transponder transponder = new Transponder(new TestDriver());
		ITestDAO testDAO = transponder.dao(ITestDAO.class);
		assertEquals("transponder.test.dao."+ITestDAO.class.getSimpleName(), testDAO.getClass().getName());
		ClassTestDAO classTestDAO = transponder.dao(ClassTestDAO.class);
		assertEquals("transponder.test.dao."+ClassTestDAO.class.getSimpleName(), classTestDAO.getClass().getName());
		ISimpleEntity simple = transponder.create(ISimpleEntity.class);
		assertEquals("transponder.test.Simple", simple.getClass().getName());
		simple = transponder.create(ISimpleEntity.class, IRemoteEntity.class);
		assertNotEquals("transponder.test.Simple", simple.getClass().getName());
		assertTrue(simple.getClass().getName().startsWith("transponder.test.Simple$"));
	}
	
	@Test
	public void testPresenceOfPredefinedParameters() {
		final String expectedName = CommonUtils.RANDOM_STRING.nextString();
		Transponder transponder = new Transponder(new TestDriver() {
			@Override
			public List<Object> query(String language, String query, Map<String, Object> params, Type type) {
				assertTrue(params.containsKey("target"));
				Map<String, Object> target = (Map<String, Object>)params.get("target");
				assertEquals(expectedName, target.get("name"));
				assertTrue(params.containsKey("targetType"));
				assertEquals("TestPredefinedParams", params.get("targetType"));
				return super.query(language, query, params, type);
			}
		});
		ITestPredefinedParams entity = transponder.create(ITestPredefinedParams.class);
		entity.setName(expectedName);
		entity.lookup();
		entity.query();
		entity.command();
	}
	
	@EntityType("TestPredefinedParams")
	public static interface ITestPredefinedParams {
		public String getName();
		public void setName(String name);
		
		@Lookup("lookup")
		ITestPredefinedParams lookup();
		
		@Lookup("query")
		ITestPredefinedParams query();
		
		@Lookup("command")
		ITestPredefinedParams command();
	}
	
	@Test
	public void testDelegation() {
		Transponder transponder = new Transponder(new TestDriver());
		Map<String, String> obj = new HashMap<>();
		assertEquals(0, obj.size());
		Map<String, String> delegator =  transponder.delegate(obj, OvverideSize.class);
		assertTrue(obj!=delegator);
		assertEquals(0, delegator.size());
		obj.put("test", "test");
		assertEquals(1, obj.size());
		assertEquals(-1, delegator.size());
		assertEquals("test", delegator.get("test"));
		assertEquals("OVERRIDED", delegator.toString());
	}
	
	public static interface OvverideSize extends ITransponderDelegator<Map<Object, Object>> {
		
		@OverrideByThis
		default int size() {
			return -get$delegate().size();
		}
		
		@OverrideByThis
		@DelegateAnnotation(OverrideSizeToStringDelegate.class)
		String toString();
	}
	
	public static class OverrideSizeToStringDelegate {
		public static String otherToString() {
			return "OVERRIDED";
		}
	}
	
}
