package org.orienteer.transponder;

import net.bytebuddy.implementation.Implementation;

public interface IDriver {
	public void createType(String typeName, boolean isAbstract, String... superTypes);
	public void createProperty(String typeName, String propertyName, String linkedType, int order);
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name);
	
	public Class<?> getGetterDelegationClass();
	public Class<?> getSetterDelegationClass();
	
	public default Object getCacheKey() {
		return getClass();
	}
	
	public <T> T newEntityInstance(Class<T> proxyClass, String type);
	
	public Class<?> getEntityBaseClass();
	
	public default <T> T newDAOInstance(Class<T> proxyClass) {
		try {
			return proxyClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Can't instanciate DAO for "+proxyClass, e);
		} 
	}
}
