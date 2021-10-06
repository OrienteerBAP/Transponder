package org.orienteer.transponder;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.orienteer.transponder.annotation.binder.PropertyName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

public class TestDriver implements IDriver {
	
	@Data
	@Accessors(chain = true)
	private static class TypeRecord {
		private String typeName;
		private boolean isAbstract;
		private String[] superTypes;
		private Map<String, PropertyRecord> properties = new HashMap<String, TestDriver.PropertyRecord>();
		
		public TypeRecord() {
		}
		
		public TypeRecord(String typeName, boolean isAbstract, String[] superTypes) {
			setTypeName(typeName).setAbstract(isAbstract).setSuperTypes(superTypes);
		}
	}
	
	@Data
	@Accessors(chain = true)
	@AllArgsConstructor
	private static class PropertyRecord {
		private String propertyName;
		private String linkedType;
		private int order;
	}
	
	private Map<String, TypeRecord> typeRecords = new HashMap<String, TestDriver.TypeRecord>();
	
	private Map<Integer, Map<String, Object>> db = new HashMap<Integer, Map<String,Object>>();

	@Override
	public void createType(String typeName, boolean isAbstract, String... superTypes) {
		typeRecords.put(typeName, new TypeRecord(typeName, isAbstract, superTypes));
	}

	@Override
	public void createProperty(String typeName, String propertyName, String linkedType, int order) {
		assertHasType(typeName);
		TypeRecord type = typeRecords.get(typeName);
		type.getProperties().put(propertyName, new PropertyRecord(propertyName, linkedType, order));
	}

	@Override
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name) {
		// TODO Auto-generated method stub
	}
	
	public void assertHasType(String typeName) {
		assertTrue("Driver has not created '"+typeName+"' yet", typeRecords.containsKey(typeName));
	}
	
	public void assertHasProperty(String typeName, String propertyName) {
		assertHasType(typeName);
		assertTrue("Driver has not created '"+typeName+"."+propertyName+"' yet", 
								typeRecords.get(typeName).getProperties().containsKey(propertyName));
	}

	@Override
	public Object getPropertyValue(Object wrapper, String property) {
		return ((Map<Object, Object>)wrapper).get(property);
	}

	@Override
	public Class<?> getSetterDelegationClass() {
		return MapSetter.class;
	}

	@Override
	public <T> T newEntityInstance(Class<T> proxyClass, String type) {
		return newDAOInstance(proxyClass);
	}
	
	@Override
	public <T> T wrapEntityInstance(Class<T> proxyClass, Object obj) {
		try {
			return proxyClass.getConstructor(Map.class).newInstance(obj);
		} catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException("Can't wrap entity ("+obj+") by class "+proxyClass.getSuperclass().getName(), e);
		}  
	}

	@Override
	public Class<?> getDefaultEntityBaseClass() {
		return HashMap.class;
	}
	
	@Override
	public Class<?> getEntityMainClass(Object object) {
		return object.getClass();
	}
	
	@Override
	public boolean isSeedClass(Class<?> seedClass) {
		return Map.class.isAssignableFrom(seedClass);
	}

	public TestDriver insertRecord(Integer pk, Map<String, Object> value) {
		db.put(pk, value);
		return this;
	}
	
	public TestDriver insertRecord(Integer pk, Object... objects) {
		return insertRecord(pk, CommonUtils.toMap(objects));
	}

	public static class MapSetter {
		@RuntimeType
		public static void setValue(@PropertyName String property, @This Map<Object, Object> thisObject, @Argument(0) Object value) {
			System.out.print("Setting: "+property +" to "+value);
			thisObject.put(property, value);
		}
	}

}
