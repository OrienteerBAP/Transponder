package org.orienteer.transponder.arcadedb;

import com.arcadedb.database.Database;
import com.arcadedb.database.Document;
import com.arcadedb.database.Identifiable;
import com.arcadedb.database.MutableDocument;

/**
 * Base class for all ArcadeDB generated wrapper classes
 */
public class DocumentWrapper {
	protected Document document;

	/**
	 * Creates empty {@link DocumentWrapper} with no associated ArcadeDB {@link Document}
	 */
	public DocumentWrapper() {
	}

	/**
	 * Creates {@link DocumentWrapper} and associate it with corresponding {@link Document}
	 * @param identifiable document or reference to the document
	 */
	public DocumentWrapper(Identifiable identifiable) {
		this.document = identifiable.asDocument();
	}

	/**
	 * Creates {@link DocumentWrapper} with newly created entity of a cpecified ArcadeDB type
	 * @param db ArcadeDB database
	 * @param typeName name of a type for new {@link Document}
	 */
	public DocumentWrapper(Database db, String typeName) {
		this(db.newDocument(typeName));
	}

	/**
	 * Sets new {@link Document} for this wrapper
	 * @param identifiable document or reference to new {@link Document}
	 */
	public void setDocument(final Identifiable identifiable) {
		this.document = identifiable.asDocument();
	}

	/**
	 * @return {@link Document} with which current wrapper is associated with
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * @return true if current {@link Document} read-only. It can be changed by calling {@link #modify()}
	 */
	public boolean isReadOnly() {
		return !(document instanceof MutableDocument);
	}

	/**
	 * Transform wrapped document to {@link MutableDocument}
	 * @return this wrapper for chaining
	 */
	public DocumentWrapper modify() {
		document = document.modify();
		return this;
	}

	/**
	 * Obtain property value
	 * @param propertyName name of a property to obtain value from
	 * @return value of the property
	 */
	public Object get(String propertyName) {
		return document.get(propertyName);
	}

	/**
	 * Sets value to a property
	 * @param properyName name of a proeprty to set value to
	 * @param value new value to set
	 * @return this wrapper for chaining 
	 */
	public DocumentWrapper set(String properyName, Object value) {
		modify();
		((MutableDocument) document).set(properyName, value);
		return this;
	}

	/**
	 * Reload associated document from the DB
	 */
	public void reload() {
		getDocument().reload();
	}

	/**
	 * Save associated document to the DB
	 */
	public void save() {
		if (document instanceof MutableDocument) {
			((MutableDocument) document).save();
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DocumentWrapper other = (DocumentWrapper) obj;
		if (document == null) {
			return other.document == null;
		} else
			return document.equals(other.document);
	}

	@Override
	public String toString() {
		return document != null ? document.toString() : "?";
	}
}
