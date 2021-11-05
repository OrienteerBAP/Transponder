package org.orienteer.transponder;

import static org.junit.Assert.*;
import static net.bytebuddy.asm.Advice.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.orienteer.transponder.IPolyglot.Translation;
import org.orienteer.transponder.Transponder.ITransponderHolder;
import org.orienteer.transponder.annotation.AdviceAnnotation;
import org.orienteer.transponder.annotation.DelegateAnnotation;
import org.orienteer.transponder.datamodel.ClassTestDAO;
import org.orienteer.transponder.datamodel.ISimpleEntity;
import org.orienteer.transponder.datamodel.ITestDAO;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType.Builder;

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
		driver.hasPropertyWithOrder("Simple", "pk", 100);
		driver.hasPropertyWithOrder("Simple", "name", 105);
		driver.hasPropertyWithOrder("Simple", "description", 110);
		driver.hasPropertyWithOrder("Simple", "value", 500);
		driver.hasPropertyWithOrder("Simple", "otherEntity", 115);
		driver.hasPropertyWithOrder("Simple", "remoteEntity", 120);
		driver.hasPropertyWithOrder("Simple", "parametrizedEntity", 125);
	}
	
}
