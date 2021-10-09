package org.orienteer.transponder.orientdb;

import org.orienteer.transponder.annotation.EntityType;

import com.orientechnologies.orient.core.metadata.schema.OClass;

@EntityType(value = "DAOTestClassRoot", isAbstract= true)
public interface IDAOTestClassRoot {

	//@DAOFieldIndex(type = OClass.INDEX_TYPE.NOTUNIQUE)
	public String getRoot();
}