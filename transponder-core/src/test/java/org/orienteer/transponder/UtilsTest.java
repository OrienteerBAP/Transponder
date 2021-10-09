package org.orienteer.transponder;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.Test;

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

}
