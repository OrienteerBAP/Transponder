package org.orienteer.transponder;

/**
 * Interface for drivers to some data sources/DBs with which Transponder can work
 */
public interface IDriver {
	/**
	 * Creates Type in underling data source. It might be OClass(OrientDB), DocumentType(ArcadeDB), etc.
	 * @param typeName name of a type to be created
	 * @param isAbstract is type should be marked as abstract?
	 * @param superTypes array of super types
	 */
	public void createType(String typeName, boolean isAbstract, String... superTypes);
	
	/**
	 * Creates property for a type.
	 * @param typeName name of a type for which new property should be created
	 * @param propertyName name of a property
	 * @param linkedType type to which this property might reference to
	 * @param order order of this property
	 */
	public void createProperty(String typeName, String propertyName, String linkedType, int order);
	
	/**
	 * Establish relationship between 2 properties from 2 types
	 * @param type1Name name of type1
	 * @param property1Name property1 on type1 which is referencing to type2
	 * @param type2Name name of type2
	 * @param property2Name property2 on type2 which is referencing to type1
	 */
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name);
	
	public Class<?> getGetterDelegationClass();
	public Class<?> getSetterDelegationClass();
	
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
	 * @param object
	 * @return
	 */
	public Class<?> getEntityMainClass(Object seed);
	
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
