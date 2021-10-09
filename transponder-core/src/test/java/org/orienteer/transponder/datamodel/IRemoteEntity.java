package org.orienteer.transponder.datamodel;

import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.annotation.Lookup;

@EntityType("Remote")
public interface IRemoteEntity {
	
	public String getRemoteName();
	public IRemoteEntity setRemoteName(String value);
	
}
