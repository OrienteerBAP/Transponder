package org.orienteer.transponder.orientdb;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.orienteer.transponder.ITestDriver;

import com.orientechnologies.orient.core.index.OIndex;
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
	public boolean hasIndex(String typeName, String indexName, String...properties) {
		if(!hasType(typeName)) return false;
		OIndex index = getSchema().getClass(typeName).getClassIndex(indexName);
		if(index==null) return false;
		List<String> fields = index.getDefinition().getFields();
		return fields.size() == properties.length && fields.containsAll(Arrays.asList(properties));
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
