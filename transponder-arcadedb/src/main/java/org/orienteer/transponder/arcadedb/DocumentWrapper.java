package org.orienteer.transponder.arcadedb;

import com.arcadedb.database.Database;
import com.arcadedb.database.Document;
import com.arcadedb.database.Identifiable;
import com.arcadedb.database.MutableDocument;

public class DocumentWrapper {
	protected Document document;

	  public DocumentWrapper() {}

	  public DocumentWrapper(Identifiable identifiable) {
	    this.document = identifiable.asDocument();
	  }

	  public DocumentWrapper(Database db, String typeName) {
	    this(db.newDocument(typeName));
	  }

	  public void setDocument(final Identifiable identifiable) {
	    this.document = identifiable.asDocument();
	  }
	  
	  public Document getDocument() {
		return document;
	  }
	  
	  public boolean isReadOnly() {
		  return !(document instanceof MutableDocument);
	  }
	  
	  public DocumentWrapper modify() {
		  document = document.modify();
		  return this;
	  }
	  
	  public Object get(String propertyName) {
		  return document.get(propertyName);
	  }
	  
	  public DocumentWrapper set(String properyName, Object value) {
		  modify();
		  ((MutableDocument)document).set(properyName, value);
		  return this;
	  }

	  public void reload() {
		  getDocument().reload();
	  }

	  public void save() {
		if(document instanceof MutableDocument) {
			((MutableDocument)document).save();
		}
	  }

	  @Override
	  public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((document == null) ? 0 : document.hashCode());
	    return result;
	  }

	  @Override
	  public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null) return false;
	    if (getClass() != obj.getClass()) return false;
	    final DocumentWrapper other = (DocumentWrapper) obj;
	    if (document == null) {
	      return other.document == null;
	    } else return document.equals(other.document);
	  }

	  @Override
	  public String toString() {
	    return document != null ? document.toString() : "?";
	  }
}
