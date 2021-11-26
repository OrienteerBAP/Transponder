package org.orienteer.transponder.mongodb;

import java.util.Map;

import org.bson.Document;
import org.orienteer.transponder.ITestDriver;
import org.orienteer.transponder.mongodb.MongoDBDriver;

import static org.orienteer.transponder.mongodb.MongoDBUtils.*;
import com.mongodb.client.MongoDatabase;

public class MongoDBTestDriver extends MongoDBDriver implements ITestDriver{
	
	public MongoDBTestDriver(MongoDatabase db) {
		super(db);
	}

	@Override
	public boolean hasType(String typeName) {
		return hasCollection(mongoDb, typeName);
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
		Document doc = new Document((Map<String, Object>)properties);
		getDatabase().getCollection(typeName).insertOne(doc);
		return doc;
	}

}
