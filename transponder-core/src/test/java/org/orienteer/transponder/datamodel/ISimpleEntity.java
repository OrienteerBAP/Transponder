package org.orienteer.transponder.datamodel;

import org.orienteer.transponder.annotation.EntityType;

@EntityType("Simple")
public interface ISimpleEntity {

	public String getName();
	public void setName(String value);
	
	public String getDescription();
	public void setDescription(String value);
	
}
