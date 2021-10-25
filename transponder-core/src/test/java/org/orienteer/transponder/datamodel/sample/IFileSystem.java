package org.orienteer.transponder.datamodel.sample;

import org.orienteer.transponder.annotation.Command;
import org.orienteer.transponder.annotation.Query;

public interface IFileSystem {
	
	@Query("select from Folder where name = :name and parent is null")
	public IFolder getRoot(String name);
	
	@Command("delete from File where content is null")
	public void removeEmpty();

}
