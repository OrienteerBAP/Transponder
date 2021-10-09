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
	 * @param mainWrapperClass TODO
	 * @param superTypes array of super types
	 */
	public void createType(String typeName, boolean isAbstract, Class<?> mainWrapperClass, String... superTypes);
	
	/**
	 * Creates property for a type.
	 * @param typeName name of a type for which new property should be created
	 * @param propertyName name of a property
	 * @param propertyType TODO
	 * @param linkedType type to which this property might reference to
	 * @param order order of this property
	 * @param annotations TODO
	 */
	public void createProperty(String typeName, String propertyName, Type propertyType, String referencedType, int order, AnnotatedElement annotations);
	
	/**
	 * Establish relationship between 2 properties from 2 types
	 * @param type1Name name of type1
	 * @param property1Name property1 on type1 which is referencing to type2
	 * @param type2Name name of type2
	 * @param property2Name property2 on type2 which is referencing to type1
	 */
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name);
	
	public Object getPropertyValue(Object wrapper, String property);
	public void setPropertyValue(Object wrapper, String property, Object value);
	
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
	 * Returns default entity base class which should be in the root of 
	 * all generated classes from this driver. Can't be interface.
	 * @return
	 */
	public Class<?> getDefaultEntityBaseClass();
	
	/**
	 * Try to obtain required main class or interface for provided seed object
	 * @param object object for which driver should try to find out
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
	 * @param seed seedClass to check
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
	 * @return list of unwrapped objects
	 */
	public List<Object> query(String language, String query, Map<String, Object> params);
	
	/**
	 * Query driver for single record from data. Should return unwrapped object or null
	 * @param language language for the query
	 * @param query query to be used
	 * @param params unwrapped parameters to be used to query data
	 * @return single data element
	 */
	public default Object querySingle(String language, String query, Map<String, Object> params) {
		List<Object> results = query(language, query, params);
		return results==null || results.isEmpty()?null:results.get(0);
	}
	
	public default Object command(String language, String command, Map<String, Object> params) {
		return query(language, command, params);
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
}
