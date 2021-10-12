package org.orienteer.transponder;

import static org.junit.Assert.assertTrue;

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
	
	public boolean hasIndex(String typeName, String indexName);
	
	public default void assertHasIndex(String typeName, String indexName) {
		assertHasType(typeName);
		assertTrue("Driver has not created '"+typeName+"."+indexName+"' yet", hasIndex(typeName, indexName));
	}
}
