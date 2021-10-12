package org.orienteer.transponder.datamodel;

import java.util.List;

import org.orienteer.transponder.annotation.Command;
import org.orienteer.transponder.annotation.DefaultValue;
import org.orienteer.transponder.annotation.Lookup;
import org.orienteer.transponder.annotation.Query;

public interface ITestDAO {
	
	
	public default Integer echoDAO(Integer number) {
		return number;
	}
	
	@Query(".*")
	public List<ISimpleEntity> getAll();
	
	@Query("${ch}{${count}}")
	public ISimpleEntity getWithCharacter(char ch, int count);
	
	@Lookup("${pk}")
	public ISimpleEntity lookupByPk(String pk);
	
	@Lookup("${pk}")
	public boolean checkPresenseByPk(String pk);
	
	@Command("${pk}")
	public ISimpleEntity removeByPk(String pk);
	
	@DefaultValue("1")
	public default Integer getDefaultValue(Integer value) {
		return value;
	}
	
//	@DefaultValue("2")
//	public Integer getDefaultValue2();
}
