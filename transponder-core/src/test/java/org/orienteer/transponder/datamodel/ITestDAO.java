package org.orienteer.transponder.datamodel;

import org.orienteer.transponder.annotation.Query;

public interface ITestDAO {
	
	public static final String QUERY = "This is query";
	
	public default Integer echoDAO(Integer number) {
		return number;
	}
	
	@Query(QUERY)
	public String queryEcho();
}
