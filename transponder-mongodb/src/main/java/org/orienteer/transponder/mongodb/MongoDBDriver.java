package org.orienteer.transponder.mongodb;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.orienteer.transponder.CommonUtils;
import org.orienteer.transponder.IDriver;
import org.orienteer.transponder.Transponder.ITransponderHolder;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import static org.orienteer.transponder.mongodb.MongoDBUtils.*;

/**
 * Transponder {@link IDriver} for MongoDB
 */
public class MongoDBDriver implements IDriver {
	
	protected final MongoDatabase mongoDb;
	
	/**
	 * Creates instance of driver for MongoDB for specified {@link MongoDatabase} 
	 * @param mongoDb Mongo database to create driver for 
	 */
	public MongoDBDriver(MongoDatabase mongoDb) {
		this.mongoDb = mongoDb;
	}

	@Override
	public void createType(String typeName, boolean isAbstract, Class<?> mainWrapperClass, String... superTypes) {
		if(!hasCollection(mongoDb, typeName))
					mongoDb.createCollection(typeName);
	}

	@Override
	public void createProperty(String typeName, String propertyName, Type propertyType, String referencedType,
			int order, AnnotatedElement annotations) {
		// TODO Implement adding validation
	}

	@Override
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name) {
		// TODO Implement adding validation
	}

	@Override
	public void createIndex(String typeName, String indexName, String indexType, AnnotatedElement annotations,
			String... properties) {
		// TODO Implement addint index		
	}

	@Override
	public Object getPropertyValue(Object wrapper, String property, Type type) {
		return ((Document)wrapper).get(property);
	}

	@Override
	public void setPropertyValue(Object wrapper, String property, Object value, Type type) {
		if(value instanceof TransponderDocument && ((TransponderDocument)value).is$persisted()) {
			ObjectId valueId = ((Document)value).getObjectId("_id");
			if(valueId!=null) value = valueId;
		}
		((Document)wrapper).put(property, value); //TODO Support of references
	}

	@Override
	public <T> T newEntityInstance(Class<T> proxyClass, String type) {
		T ret = CommonUtils.newInstance(proxyClass);
		((TransponderDocument)ret).set$collection(CommonUtils.resolveEntityType(proxyClass));
		return ret;
	}

	@Override
	public void saveEntityInstance(Object wrapper) {
		saveTransponderDocument((TransponderDocument)wrapper, new LinkedList<TransponderDocument>());
	}
	
	protected void saveTransponderDocument(TransponderDocument doc, List<TransponderDocument> persisted) {
		if(persisted.contains(doc)) return;
		persisted.add(doc);
		doc.forEach((k, v) -> {
			if(v instanceof TransponderDocument) saveTransponderDocument((TransponderDocument)v, persisted);
		});
		MongoCollection<Document> collection =
				getDatabase().getCollection(doc.get$collection());
		ObjectId objectId = doc.getObjectId("_id");
		if(objectId==null) {
			doc.put("_id", ObjectId.get());
			collection.insertOne(doc);
		} else {
			collection.updateOne(new Document("_id", objectId), doc, new UpdateOptions().upsert(true));
		}
		
		doc.set$persisted(true);
	}

	@Override
	public <T> T wrapEntityInstance(Class<T> proxyClass, Object seed) {
		String collectionName = CommonUtils.resolveEntityType(proxyClass);
		if(seed instanceof ObjectId) {
			seed = getDatabase().getCollection(collectionName)
 					.find(new Document("_id", seed)).limit(1).first();
		}
		
		if(seed instanceof Document) {
			try {
				T ret = (T) proxyClass.getConstructor(Map.class).newInstance(seed);
				((TransponderDocument)ret).set$collection(collectionName);
				return ret;
			} catch (Exception e) {
				throw new IllegalArgumentException("Can't instantiate "+proxyClass.getName());
			} 
		} else return null;
	}

	@Override
	public Class<?> getDefaultEntityBaseClass() {
		return TransponderDocument.class;
	}

	@Override
	public Class<?> getEntityMainClass(Object seed) {
		return null;
	}

	@Override
	public boolean isSeedClass(Class<?> seedClass) {
		return (Document.class.isAssignableFrom(seedClass) && !ITransponderHolder.class.isAssignableFrom(seedClass))
				|| ObjectId.class.isAssignableFrom(seedClass);
	}

	@Override
	public Object toSeed(Object wrapped) {
		TransponderDocument doc = (TransponderDocument)wrapped;
		if(doc.is$persisted()) return doc.getObjectId("_id");
		else return new TransponderDocument(doc);
	}

	@Override
	public List<Object> query(String language, String query, Map<String, Object> params, Type type) {
		String collectionName = CommonUtils.resolveEntityType(CommonUtils.typeToRequiredClass(type));
		FindIterable<Document> results = (FindIterable<Document>)MongoDBUtils
										.execute(getDatabase(), query, params, collectionName);
		return StreamSupport.stream(results.spliterator(), false)
					.collect(Collectors.toList());
	}
	
	@Override
	public Object querySingle(String language, String query, Map<String, Object> params, Type type) {
		String collectionName = CommonUtils.resolveEntityType(CommonUtils.typeToRequiredClass(type));
		FindIterable<Document> results = (FindIterable<Document>)MongoDBUtils
										.execute(getDatabase(), query, params, collectionName);
		return results.limit(1).first();
	}
	
	@Override
	public Object command(String language, String command, Map<String, Object> params, Type type) {
		String collectionName = CommonUtils.resolveEntityType(CommonUtils.typeToRequiredClass(type));
		Object results = MongoDBUtils.execute(getDatabase(), command, params, collectionName);
		if(results instanceof FindIterable) {
			return StreamSupport.stream(((FindIterable<Document>)results).spliterator(), false)
					.collect(Collectors.toList());
		} else {
			return results;
		}
	}
	
	
	@Override
	public void replaceSeed(Object wrapper, Object newSeed) {
		if(newSeed instanceof ObjectId) {
			newSeed = getDatabase().getCollection(CommonUtils.resolveEntityType(wrapper.getClass()))
						.find(new Document("_id", newSeed)).limit(1).first();
		}
		if(newSeed instanceof Document) {
			Document doc = (Document) wrapper;
			doc.clear();
			doc.putAll((Document)newSeed);
		}
	}

	@Override
	public String getDialect() {
		return "mongodb";
	}
	
	/**
	 * Returns {@link MongoDatabase}
	 * @return instance of {@link MongoDatabase}
	 */
	public MongoDatabase getDatabase() {
		return mongoDb;
	}
	
	
}
