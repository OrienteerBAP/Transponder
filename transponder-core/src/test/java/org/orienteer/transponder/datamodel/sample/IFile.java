package org.orienteer.transponder.datamodel.sample;

import org.orienteer.transponder.annotation.EntityType;

@EntityType("File")
public interface IFile extends IEntry {

	public byte[] getContent();
	public void setContent(byte[] value);
	
}
