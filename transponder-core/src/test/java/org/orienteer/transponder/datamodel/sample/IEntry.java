package org.orienteer.transponder.datamodel.sample;


import org.orienteer.transponder.annotation.EntityIndex;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.annotation.Lookup;

@EntityType("Entry")
@EntityIndex(name = "nameParent", properties = {"name", "parent"})
public interface IEntry {

	public String getName();
	public void setName(String value);
	
	public IFolder getParent();
	public void setParent(IFolder value);
	
	@Lookup("select from Entry where name=:name and parent=:parent")
	public boolean lookupByName(String name, IFolder parent);
}
