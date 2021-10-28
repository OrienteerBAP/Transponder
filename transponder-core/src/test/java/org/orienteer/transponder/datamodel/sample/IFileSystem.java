package org.orienteer.transponder.datamodel.sample;

import java.util.List;

import org.orienteer.transponder.annotation.Command;
import org.orienteer.transponder.annotation.Lookup;
import org.orienteer.transponder.annotation.Query;

public interface IFileSystem {
	
	@Query("select from Folder where name = :name and parent is null")
	public IFolder getRoot(String name);
	
	@Query("select from Entry where name=:name and parent=:parent")
	public IEntry lookupByName(String name, IFolder parent);
	
	@Query("select from Entry where name like :search")
	public List<IEntry> search(String search);
	
	@Command("delete from File where content is null")
	public void removeEmpty();
}
