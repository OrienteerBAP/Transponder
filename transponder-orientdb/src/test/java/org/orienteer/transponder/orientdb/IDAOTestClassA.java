package org.orienteer.transponder.orientdb;

import java.util.List;

import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityType;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

@EntityType("DAOTestClassA")
//@DAOIndex(name="rootname", type=OClass.INDEX_TYPE.NOTUNIQUE, fields = {"name", "root"})
public interface IDAOTestClassA extends IDAOTestClassRoot{


	public String getName();
	public void setName(String name);
	
	public IDAOTestClassB getBSingle();
	
	@EntityProperty("bOtherField")
	public IDAOTestClassB getBOther();
	
	
	public List<String> getEmbeddedStringList();
	
	@OrientDBProperty(linkedClass = "DAOTestClassB")
	public ODocument getLinkAsDoc();
	
	@OrientDBProperty(linkedClass = "DAOTestClassB")
	public List<ODocument> getLinkList();
	public IDAOTestClassA getSelfType();
}
