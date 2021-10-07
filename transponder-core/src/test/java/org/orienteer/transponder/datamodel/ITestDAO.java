package org.orienteer.transponder.datamodel;

import java.util.List;

import org.orienteer.transponder.annotation.Query;

public interface ITestDAO {
	
	
	public default Integer echoDAO(Integer number) {
		return number;
	}
	
	@Query(".*")
	public List<ISimpleEntity> getAll();
	
	@Query("${ch}{${count}}")
	public ISimpleEntity getWithCharacter(char ch, int count);
}
