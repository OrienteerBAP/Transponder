package org.orienteer.transponder.dao;

public interface ITestDAO {
	
	public default Integer echoDAO(Integer number) {
		return number;
	}
}
