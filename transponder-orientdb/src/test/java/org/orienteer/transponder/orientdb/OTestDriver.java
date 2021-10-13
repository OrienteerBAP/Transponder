package org.orienteer.transponder.orientdb;

import java.util.Map;

import org.orienteer.transponder.ITestDriver;

import com.orientechnologies.orient.core.record.impl.ODocument;

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

	@Override
	public Object createSeedObject(String typeName, Map<String, ?> properties) {
		ODocument doc = new ODocument(typeName);
		for (Map.Entry<String, ?> entry : properties.entrySet()) {
			doc.field(entry.getKey(), entry.getValue());
		}
		return doc;
	}
	
	

}
