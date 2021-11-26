package org.orienteer.transponder.neo4j;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.orienteer.transponder.IDriver;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Transponder {@link IDriver} for Neo4J
 */
public class Neo4JDriver implements IDriver {
	
	protected class TransactionHolder implements AutoCloseable {
		
		private Transaction transaction;
		
		public Transaction getTransaction() {
			if(externalTransaction!=null) return externalTransaction;
			else if(transaction==null) {
				transaction = database.beginTx();
			}
			return transaction;
		}

		@Override
		public void close() {
			if(transaction!=null) {
				transaction.commit();
				transaction.close();
			}
		}
		
	}
	
	public static final String DIALECT_NEO4J = "neo4j";
	
	public static final String TYPE_CUSTOM_TRANSPONDER_WRAPPER = "transponder.wrapper";
	
	private static final BiMap<String, Class<?>> TYPE_TO_MAIN_CLASS = HashBiMap.create(); 
	
	private final GraphDatabaseService database;
	
	private Transaction externalTransaction;
	
	/**
	 * Creates {@link IDriver} which associated with provided Neo4J database
	 * @param database Neo4J database instance to associate driver with
	 */
	public Neo4JDriver(GraphDatabaseService database) {
		this.database = database;
	}
	
	/**
	 * Creates {@link IDriver} which associated with provided Neo4J database and transaction
	 * @param database Neo4J database instance to associate driver with
	 * @param externalTransaction external transaction to associate with
	 */
	public Neo4JDriver(GraphDatabaseService database, Transaction externalTransaction) {
		this.database = database;
		this.externalTransaction = externalTransaction;
	}
	
	@Override
	public void createType(String typeName, boolean isAbstract, Class<?> mainWrapperClass, String... superTypes) {
		TYPE_TO_MAIN_CLASS.put(typeName, mainWrapperClass);
	}

	@Override
	public void createProperty(String typeName, String propertyName, Type propertyType, String referencedType,
			int order, AnnotatedElement annotations) {
		//NOP for Neo4J
	}

	@Override
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name) {
		//NOP for Neo4J
	}

	@Override
	public void createIndex(String typeName, String indexName, String indexType, AnnotatedElement annotations,
			String... properties) {
		/*try(TransactionHolder txHolder = new TransactionHolder()) {
			txHolder.getTransaction().schema().indexFor((Label)null).
		}*/
	}

	@Override
	public Object getPropertyValue(Object wrapper, String property, Type type) {
		return ((EntityWrapper)wrapper).get(property, type);
	}

	@Override
	public void setPropertyValue(Object wrapper, String property, Object value, Type type) {
		((EntityWrapper)wrapper).set(property, value, type);
	}

	@Override
	public <T> T newEntityInstance(Class<T> proxyClass, String type) {
		try(TransactionHolder holder = new TransactionHolder()) {
			return wrapEntityInstance(proxyClass, holder.getTransaction().createNode(Label.label(type)));
		}
	}

	@Override
	public void saveEntityInstance(Object wrapper) {
		//NEO4J auto-save
	}

	@Override
	public <T> T wrapEntityInstance(Class<T> proxyClass, Object seed) {
		try {
			return proxyClass.getConstructor(Entity.class).newInstance((Entity)seed);
		}catch (Exception e) {
			throw new IllegalArgumentException("Can't wrap seed by class "+proxyClass+". Seed: "+seed, e);
		}
	}

	@Override
	public Class<?> getDefaultEntityBaseClass() {
		return EntityWrapper.class;
	}

	@Override
	public Class<?> getEntityMainClass(Object seed) {
		List<String> types = Neo4JUtils.entityToType((Entity)seed);
		for (String type : types) {
			Class<?> mainClass = TYPE_TO_MAIN_CLASS.get(type);
			if(mainClass!=null) return mainClass;
		}
		return null;
	}

	@Override
	public boolean isSeedClass(Class<?> seedClass) {
		return Entity.class.isAssignableFrom(seedClass);
	}

	@Override
	public Object toSeed(Object wrapped) {
		return ((EntityWrapper)wrapped).getEntity();
	}

	@Override
	public List<Object> query(String language, String query, Map<String, Object> params, Type type) {
		try(TransactionHolder holder = new TransactionHolder()) {
			Transaction tx = holder.getTransaction();
			try(Result result = tx.execute(query, params)) {
				List<String> columns = result.columns();
				return columns.size()>0
							?result.columnAs(columns.get(0)).stream().collect(Collectors.toList())
							:null;
			}
		}
	}

	@Override
	public String getDialect() {
		return DIALECT_NEO4J;
	}
	
	@Override
	public void replaceSeed(Object wrapper, Object newSeed) {
		((EntityWrapper)wrapper).setEntity((Entity)newSeed);
	}

	/**
	 * @return associated Neo4J {@link GraphDatabaseService} instance
	 */
	public GraphDatabaseService getDatabase() {
		return database;
	}
	
}
