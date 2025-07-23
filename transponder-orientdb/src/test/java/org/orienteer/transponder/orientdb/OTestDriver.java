package org.orienteer.transponder.orientdb;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.orienteer.transponder.CommonUtils;
import org.orienteer.transponder.ITestDriver;

import com.google.common.base.Objects;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class OTestDriver extends ODriver implements ITestDriver {
	
	@Override
	protected ODatabaseSession getSession() {
		return OrientDBUniversalTest.getODatabaseSession();
	}

	@Override
	public boolean hasType(String typeName) {
		return getSchema().getClass(typeName) !=null;
	}

	@Override
	public boolean hasProperty(String typeName, String propertyName) {
		return hasType(typeName) && getSchema().getClass(typeName).getProperty(propertyName)!=null;
	}
	
	@Override
	public boolean hasReferenceProperty(String typeName, String propertyName, String referenceType) {
		if(!hasProperty(typeName, propertyName)) return false;
		OClass referencedClass = getSchema().getClass(typeName).getProperty(propertyName).getLinkedClass();
		return Objects.equal(referenceType, referencedClass!=null?referencedClass.getName():null);
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
		doc.save();
		return doc;
	}
	
	

}
