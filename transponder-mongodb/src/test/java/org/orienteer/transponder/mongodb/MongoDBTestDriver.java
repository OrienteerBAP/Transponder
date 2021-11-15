package org.orienteer.transponder.mongodb;

import java.util.Map;

import org.orienteer.transponder.ITestDriver;
import org.orienteer.transponder.mongodb.MongoDBDriver;

import com.mongodb.client.MongoDatabase;

public class MongoDBTestDriver extends MongoDBDriver implements ITestDriver{
	
	public MongoDBTestDriver(MongoDatabase db) {
		super(db);
	}

	@Override
	public boolean hasType(String typeName) {
		return true;
	}

	@Override
	public boolean hasProperty(String typeName, String propertyName) {
		return true;
	}

	@Override
	public boolean hasReferenceProperty(String typeName, String propertyName, String referenceType) {
		return true;
	}

	@Override
	public boolean hasIndex(String typeName, String indexName, String... properties) {
		return true;
	}

	@Override
	public Object createSeedObject(String typeName, Map<String, ?> properties) {
		// TODO Auto-generated method stub
		return null;
	}

}
