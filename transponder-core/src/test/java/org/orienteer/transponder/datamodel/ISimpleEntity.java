package org.orienteer.transponder.datamodel;

import org.orienteer.transponder.annotation.Command;
import org.orienteer.transponder.annotation.DefaultValue;
import org.orienteer.transponder.annotation.EntityIndex;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityPropertyIndex;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.annotation.Lookup;

@EntityType(value = "Simple", orderOffset = 100, orderStep = 5)
@EntityIndex(name = "nameDescription", properties = {"name", "description"})
@EntityIndex(name = "nameValue", properties = {"name", "value"})
public interface ISimpleEntity {
	
	public static final String defaultValue = "Default Value";
	
	public String getPk();
	public ISimpleEntity setPk(String pk);

	public String getName();
	public ISimpleEntity setName(String value);
	
	public String getDescription();
	public void setDescription(String value);
	
	@DefaultValue("EMPTY")
	@EntityPropertyIndex
	@EntityProperty(order=500)
	public String getValue();
	public void setValue(String value);
	
	public ISimpleEntity getOtherEntity();
	public void setOtherEntity(ISimpleEntity entity);
	
	public IRemoteEntity getRemoteEntity();
	public void setRemoteEntity(IRemoteEntity entity);
	
	public IParametrizedEntity<ISimpleEntity> getParametrizedEntity();
	public void setParametrizedEntity(IParametrizedEntity<ISimpleEntity> entity);
	
	@Lookup(id="byPk")
	public ISimpleEntity lookupByPk(String pk);
	
	@Lookup(id="byPk")
	public boolean checkPresenseByPk(String pk);
	
	public default String getDefault() {
		return defaultValue;
	}
	
	public default String setDefault(String value) {
		return value;
	}
	
}
