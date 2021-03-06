package org.orienteer.transponder;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


/**
 * Interface for drivers to some data sources/DBs with which Transponder can work
 */
public interface IDriver {
	/**
	 * Creates Type in underling data source. It might be OClass(OrientDB), DocumentType(ArcadeDB), etc.
	 * @param typeName name of a type to be created
	 * @param isAbstract is type should be marked as abstract?
	 * @param mainWrapperClass interface class name to be associated with this type which is being created
	 * @param superTypes array of super types
	 */
	public void createType(String typeName, boolean isAbstract, Class<?> mainWrapperClass, String... superTypes);
	
	/**
	 * Invoked when all properties and indexes have been created.
	 * Useful if something needs to be done with properties
	 * @param typeName name of just created type
	 * @param mainWrapperClass class which was used for type creation. It might contain useful annotations
	 */
	public default void onPostCreateType(String typeName, Class<?> mainWrapperClass) {
		
	}
	
	/**
	 * Creates property for a type.
	 * @param typeName name of a type for which new property should be created
	 * @param propertyName name of a property
	 * @param propertyType type of the property to be created
	 * @param referencedType type to which this property might reference to.
	 * 		It might not exist yet. In this case - do nothing and update in {@link #setupRelationship(String, String, String, String)}
	 * @param order order of this property
	 * @param annotations additional annotations which might be helpful for customized creation a type in the DB
	 */
	public void createProperty(String typeName, String propertyName, Type propertyType, String referencedType, int order, AnnotatedElement annotations);
	
	/**
	 * Establish relationship between 2 properties from 2 types
	 * @param type1Name name of type1
	 * @param property1Name property1 on type1 which is referencing to type2
	 * @param type2Name name of type2
	 * @param property2Name property2 on type2 which is referencing to type1. Might be null if it's one way setup
	 */
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name);
	
	/**
	 * Creates index on specified type and for the set of properties.
	 * Type of index depends on particular driver.
	 * @param typeName name of a type to create index on
	 * @param indexName name of the index to create
	 * @param indexType type of the index to be created
	 * @param annotations element to check for possible additional annotations
	 * @param properties set of properties to be included into the index
	 */
	public void createIndex(String typeName, String indexName, String indexType, AnnotatedElement annotations, String... properties);
	
	/**
	 * Get value of an property
	 * @param wrapper wrapper object to get property value from. If unwrapping is needed: driver can through {@link #toSeed(Object)}
	 * @param property name of a property to obtain value for
	 * @param type expected return type. The driver can ignore it and allow transponder do the magic
	 * @return value of an property
	 */
	public Object getPropertyValue(Object wrapper, String property, Type type);
	
	/**
	 * Sets value to a property
	 * @param wrapper wrapper object to set property value to. If unwrapping is needed: driver can through {@link #toSeed(Object)}
	 * @param property name of a property to set value to
	 * @param value actual value to set
	 * @param type setter type. The driver can ignore it, because value should be already unwrapped
	 */
	public void setPropertyValue(Object wrapper, String property, Object value, Type type);
	
	/**
	 * Providing key for this instance of driver which will be used for caching of generated classes.
	 * Be default key is defined by class of a driver.
	 * @return key to be used in cache key calculation
	 */
	public default Object getCacheKey() {
		return getClass();
	}
	
	/**
	 * Create new wrapped entity with provided type
	 * @param <T> wrapper class
	 * @param proxyClass proxy class to wrap into
	 * @param type type of an entity to be created
	 * @return wrapped just created instance of requested type
	 */
	public <T> T newEntityInstance(Class<T> proxyClass, String type);
	
	/**
	 * Persist object in an DB
	 * @param wrapper entity to persist
	 */
	public void saveEntityInstance(Object wrapper);
	
	/**
	 * Try to replace seed object for provided wrapper object. Useful for Lookup operations.
	 * @param wrapper wrapper to replace seed in 
	 * @param newSeed new seed object
	 */
	public default void replaceSeed(Object wrapper, Object newSeed) {
		throw new UnsupportedOperationException("Replacing of seeds is not supported by current driver");
	}
	
	/**
	 * Wrap provided original object into provided proxy class
	 * @param <T> wrapper class
	 * @param proxyClass proxy class to wrap into
	 * @param seed object to be wrapped
	 * @return wrapped original object
	 */
	public <T> T wrapEntityInstance(Class<T> proxyClass, Object seed);
	
	/**
	 * @return default entity base class which should be in the root of 
	 * all generated classes from this driver. Can't be interface.
	 */
	public Class<?> getDefaultEntityBaseClass();
	
	/**
	 * Try to obtain required main class or interface for provided seed object
	 * @param seed object for which driver should try to find out main class
	 * @return most appropriate class for wrapping or null
	 */
	public Class<?> getEntityMainClass(Object seed);
	
	/**
	 * Is object a seed? In other word: can it be wrapped by current driver?
	 * @param seed object to check
	 * @return true if object can wrapped, false - if driver doesn't support such object seed
	 */
	public default boolean isSeed(Object seed) {
		return seed!=null?isSeedClass(seed.getClass()):false;
	}
	
	/**
	 * Is objects of this class can be a seed?
	 * In other word: can instances of this class be wrapped by current driver?
	 * @param seedClass candidate class which might be suitable for seed objects
	 * @return true if object can wrapped, false - if driver doesn't support such object seed
	 */
	public boolean isSeedClass(Class<?> seedClass);
	
	/**
	 * Extract seed object from specified wrapped object
	 * @param wrapped wrapped object to extract seed from
	 * @return seed object which can be used to interact with underling DBs
	 */
	public Object toSeed(Object wrapped);
	
	/**
	 * Query driver for data. Should return unwrapped list
	 * @param language language for the query
	 * @param query query to be used
	 * @param params unwrapped parameters to be used to query data
	 * @param type expected return type
	 * @return list of unwrapped objects
	 */
	public List<Object> query(String language, String query, Map<String, Object> params, Type type);
	
	/**
	 * Query driver for single record from data. Should return unwrapped object or null
	 * @param language language for the query
	 * @param query query to be used
	 * @param params unwrapped parameters to be used to query data
	 * @param type expected return type
	 * @return single data element
	 */
	public default Object querySingle(String language, String query, Map<String, Object> params, Type type) {
		List<Object> results = query(language, query, params, type);
		return results==null || results.isEmpty()?null:results.get(0);
	}
	
	/**
	 * Execute specified command 
	 * @param language language for the command
	 * @param command command to be used
	 * @param params unwrapped parameters to be used to query data
	 * @param type expected return type
	 * @return return of the command
	 */
	public default Object command(String language, String command, Map<String, Object> params, Type type) {
		return query(language, command, params, type);
	}
	
	/**
	 * Creates new instance of a DAO object from provided proxy class
	 * @param <T> class of the DAO
	 * @param proxyClass proxy class which generated for the requested DAO
	 * @return instance of newly created DAO
	 */
	public default <T> T newDAOInstance(Class<T> proxyClass) {
		try {
			return proxyClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Can't instanciate DAO for "+proxyClass, e);
		} 
	}
	
	/**
	 * Provide driver specific mutator for building a proxy class
	 * @return mutator to be used during building a class
	 */
	public default IMutator getMutator() {
		return null;
	}
	
	/**
	 * @return dialect name supported by driver
	 */
	public String getDialect();
}
