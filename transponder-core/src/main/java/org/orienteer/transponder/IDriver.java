package org.orienteer.transponder;

public interface IDriver {
	public void createType(String typeName, boolean isAbstract, String... superTypes);
	public void createProperty(String typeName, String propertyName, String linkedType, int order);
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name);
	
	public default <T> T newDAOInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Can't instanciate DAO for "+clazz, e);
		} 
	}
}
