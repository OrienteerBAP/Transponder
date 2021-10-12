package org.orienteer.transponder.orientdb;

import org.orienteer.transponder.ITestDriver;

public class OTestDriver extends ODriver implements ITestDriver {

	@Override
	public boolean hasType(String typeName) {
		return getSchema().getClass(typeName) !=null;
	}

	@Override
	public boolean hasProperty(String typeName, String propertyName) {
		return hasType(typeName) && getSchema().getClass(typeName).getProperty(propertyName)!=null;
	}

	@Override
	public boolean hasIndex(String typeName, String indexName) {
		return hasType(typeName) && getSchema().getClass(typeName).getClassIndex(indexName)!=null;
	}

}
