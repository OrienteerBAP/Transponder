package org.orienteer.transponder.datamodel;

import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.annotation.Lookup;

@EntityType("Simple")
public interface ISimpleEntity {
	
	public static final String defaultValue = "Default Value";

	public String getName();
	public ISimpleEntity setName(String value);
	
	public String getDescription();
	public void setDescription(String value);
	
	public ISimpleEntity getOtherEntity();
	public void setOtherEntity(ISimpleEntity entity);
	
	public IRemoteEntity getRemoteEntity();
	public void setRemoteEntity(IRemoteEntity entity);
	
	public IParametrizedEntity<ISimpleEntity> getParametrizedEntity();
	public void setParametrizedEntity(IParametrizedEntity<ISimpleEntity> entity);
	
	@Lookup("${pk}")
	public ISimpleEntity lookupByPk(String pk);
	
	@Lookup("${pk}")
	public boolean checkPresenseByPk(String pk);
	
	public default String getDefault() {
		return defaultValue;
	}
	
	public default String setDefault(String value) {
		return value;
	}
	
}
