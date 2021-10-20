package org.orienteer.transponder.datamodel.sample;

import java.util.List;

import org.orienteer.transponder.annotation.EntityType;

@EntityType("Folder")
public interface IFolder extends IEntry {

	public List<IEntry> getChild();
	public void setChild(List<IEntry> value);
}
