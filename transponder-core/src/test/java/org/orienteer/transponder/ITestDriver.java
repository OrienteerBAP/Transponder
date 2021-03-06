package org.orienteer.transponder;

import static org.junit.Assert.assertTrue;

import java.util.Map;

public interface ITestDriver extends IDriver {
	
	public boolean hasType(String typeName);
	
	public default void assertHasType(String typeName) {
		assertTrue("Driver has not created '"+typeName+"' yet", hasType(typeName));
	}
	
	public boolean hasProperty(String typeName, String propertyName);
	
	public default void assertHasProperty(String typeName, String propertyName) {
		assertHasType(typeName);
		assertTrue("Driver has not created '"+typeName+"."+propertyName+"' yet", 
								hasProperty(typeName, propertyName));
	}
	
	public boolean hasReferenceProperty(String typeName, String propertyName, String referenceType);
	
	public default void assertHasReferenceProperty(String typeName, String propertyName, String referenceType) {
		assertHasProperty(typeName, propertyName);
		assertTrue("Driver has not expected reference type for property'"+typeName+"."+propertyName+"'. Expects: "+referenceType, 
				hasReferenceProperty(typeName, propertyName, referenceType));
	}
	
	public boolean hasIndex(String typeName, String indexName, String...properties);
	
	public default void assertHasIndex(String typeName, String indexName, String... properties) {
		assertHasType(typeName);
		assertTrue("Driver has not created '"+typeName+"."+indexName+"' yet", hasIndex(typeName, indexName, properties));
	}
	
	public Object createSeedObject(String typeName, Map<String, ?> properties);
}
