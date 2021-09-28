package org.orienteer.transponder;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

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

}
