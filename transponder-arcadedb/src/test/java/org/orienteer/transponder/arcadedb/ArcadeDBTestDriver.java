package org.orienteer.transponder.arcadedb;

import java.util.Map;

import org.orienteer.transponder.ITestDriver;

import com.arcadedb.database.Database;
import com.arcadedb.database.MutableDocument;

public class ArcadeDBTestDriver extends ArcadeDBDriver implements ITestDriver{
	
	

	public ArcadeDBTestDriver(Database database, boolean overrideSchema) {
		super(database, overrideSchema);
	}

	public ArcadeDBTestDriver(Database database) {
		super(database);
	}

	@Override
	public boolean hasType(String typeName) {
		return getSchema().existsType(typeName);
	}

	@Override
	public boolean hasProperty(String typeName, String propertyName) {
		return hasType(typeName) && getSchema().getType(typeName).existsPolymorphicProperty(propertyName);
	}

	@Override
	public boolean hasIndex(String typeName, String indexName, String...properties) {
		return hasType(typeName) 
				&& getSchema().getType(typeName).getPolymorphicIndexByProperties(properties)!=null;
	}

	@Override
	public Object createSeedObject(String typeName, Map<String, ?> properties) {
		MutableDocument doc = getDatabase().newDocument(typeName);
		doc.merge((Map<String, Object>)properties);
		doc.save();
		return doc;
	}

}
