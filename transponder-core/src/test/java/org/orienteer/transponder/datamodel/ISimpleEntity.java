package org.orienteer.transponder.datamodel;

import org.orienteer.transponder.annotation.EntityType;

@EntityType("Simple")
public interface ISimpleEntity {
	
	public static final String defaultValue = "Default Value";

	public String getName();
	public void setName(String value);
	
	public String getDescription();
	public void setDescription(String value);
	
	public default String getDefault() {
		return defaultValue;
	}
	
	public default String setDefault(String value) {
		return value;
	}
	
}
