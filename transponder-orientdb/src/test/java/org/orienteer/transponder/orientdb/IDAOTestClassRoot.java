package org.orienteer.transponder.orientdb;

import org.orienteer.transponder.annotation.EntityPropertyIndex;
import org.orienteer.transponder.annotation.EntityType;

import com.orientechnologies.orient.core.metadata.schema.OClass;

@EntityType(value = "DAOTestClassRoot", isAbstract= true)
public interface IDAOTestClassRoot {

	@EntityPropertyIndex(type = ODriver.OINDEX_NOTUNIQUE)
	public String getRoot();
}