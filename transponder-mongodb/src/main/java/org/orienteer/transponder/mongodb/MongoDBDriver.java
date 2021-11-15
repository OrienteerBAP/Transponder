package org.orienteer.transponder.mongodb;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.orienteer.transponder.IDriver;

import com.mongodb.client.MongoDatabase;

/**
 * Transponder {@link IDriver} for MongoDB
 */
public class MongoDBDriver implements IDriver {
	
	private final MongoDatabase mongoDb;
	
	public MongoDBDriver(MongoDatabase mongoDb) {
		this.mongoDb = mongoDb;
	}

	@Override
	public void createType(String typeName, boolean isAbstract, Class<?> mainWrapperClass, String... superTypes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createProperty(String typeName, String propertyName, Type propertyType, String referencedType,
			int order, AnnotatedElement annotations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createIndex(String typeName, String indexName, String indexType, AnnotatedElement annotations,
			String... properties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getPropertyValue(Object wrapper, String property, Type type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPropertyValue(Object wrapper, String property, Object value, Type type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> T newEntityInstance(Class<T> proxyClass, String type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveEntityInstance(Object wrapper) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> T wrapEntityInstance(Class<T> proxyClass, Object seed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getDefaultEntityBaseClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getEntityMainClass(Object seed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSeedClass(Class<?> seedClass) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object toSeed(Object wrapped) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> query(String language, String query, Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDialect() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
