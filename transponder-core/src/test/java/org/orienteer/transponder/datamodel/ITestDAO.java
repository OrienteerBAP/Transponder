package org.orienteer.transponder.datamodel;

public interface ITestDAO {
	
	public default Integer echoDAO(Integer number) {
		return number;
	}
}
