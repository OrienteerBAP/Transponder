package org.orienteer.transponder.orientdb;

import java.util.List;

import org.orienteer.transponder.annotation.EntityIndex;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityType;

import com.orientechnologies.orient.core.record.impl.ODocument;

@EntityType("DAOTestClassA")
@EntityIndex(name="rootname", type=ODriver.OINDEX_NOTUNIQUE, properties = {"name", "root"})
public interface IDAOTestClassA extends IDAOTestClassRoot{


	public String getName();
	public void setName(String name);
	
	public IDAOTestClassB getBSingle();
	
	@EntityProperty("bOtherField")
	public IDAOTestClassB getBOther();
	
	
	public List<String> getEmbeddedStringList();
	
	@EntityProperty(value = "linkAsDoc", referencedType = "DAOTestClassB")
	public ODocument getLinkAsDoc();
	
	@EntityProperty(value = "linkList", referencedType = "DAOTestClassB")
	public List<ODocument> getLinkList();
	public IDAOTestClassA getSelfType();
}
