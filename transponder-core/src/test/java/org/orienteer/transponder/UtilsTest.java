package org.orienteer.transponder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class UtilsTest {
	
	private static class Sample {
		public Integer integer;
		public int simpleInt;
		public List<Integer> list;
		public Map<Integer, String> map;
	}
	
	private Type getType(String fieldName) throws Exception {
		return Sample.class.getField(fieldName).getGenericType();
	}
	
	@Test
	public void testGetMasterClass() throws Exception {
		assertEquals(Integer.class, CommonUtils.typeToMasterClass(getType("integer")));
		assertEquals(Integer.class, CommonUtils.typeToMasterClass(getType("simpleInt")));
		assertEquals(List.class, CommonUtils.typeToMasterClass(getType("list")));
		assertEquals(Map.class, CommonUtils.typeToMasterClass(getType("map")));
	}
	
	@Test
	public void testGetRequiredClass() throws Exception {
		assertEquals(Integer.class, CommonUtils.typeToRequiredClass(getType("integer")));
		assertEquals(Integer.class, CommonUtils.typeToRequiredClass(getType("simpleInt")));
		assertEquals(Integer.class, CommonUtils.typeToRequiredClass(getType("list")));
		assertEquals(String.class, CommonUtils.typeToRequiredClass(getType("map")));
	}
	
	/** Java 17+ class file features (PermittedSubclasses) must not break reading the source order */
	public sealed interface ISealedEntity permits ISealedEntityChild {
		public String getName();
		public void setName(String value);
		public Integer getValue();
	}
	
	public non-sealed interface ISealedEntityChild extends ISealedEntity {
	}
	
	@Test
	public void testListDeclaredMethodsOfSealedInterface() throws Exception {
		List<Method> methods = CommonUtils.listDeclaredMethods(ISealedEntity.class);
		assertEquals(3, methods.size());
		assertEquals("getName", methods.get(0).getName());
		assertEquals("setName", methods.get(1).getName());
		assertEquals("getValue", methods.get(2).getName());
	}

}
