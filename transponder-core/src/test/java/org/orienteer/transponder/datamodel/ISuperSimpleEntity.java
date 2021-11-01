package org.orienteer.transponder.datamodel;

public interface ISuperSimpleEntity {
	
	public static interface IAnotherInterface {
		
	}

	public String getPk();
	public ISimpleEntity setPk(String pk);

	public String getName();
	public ISimpleEntity setName(String value);
	
}
