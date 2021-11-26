package org.orienteer.transponder.mongodb;

import java.util.Map;

import org.bson.Document;

/**
 * Extension of Document for transponder to store additional data about actual collection name
 * and flag weather this document has been persisted or not 
 *
 */
public class TransponderDocument extends Document {
	
	private String collection;
	private boolean persisted = false;

	/**
	 * Creates default {@link TransponderDocument}
	 */
	public TransponderDocument() {
	}
	
	/**
	 * Creates {@link TransponderDocument} as cleaner clone of specified {@link TransponderDocument}
	 * @param doc {@link TransponderDocument} to clone from
	 */
	public TransponderDocument(TransponderDocument doc) {
		super(doc);
		this.persisted = doc.persisted;
		this.collection = doc.collection;
	}

	/**
	 * Creates {@link TransponderDocument} from Map
	 * @param map initial properties map
	 */
	public TransponderDocument(Map<String, Object> map) {
		super(map);
	}

	/**
	 * Creates {@link TransponderDocument} with just one key-value 
	 * @param key key 
	 * @param value value
	 */
	public TransponderDocument(String key, Object value) {
		super(key, value);
	}
	
	//CHECKSTYLE IGNORE MethodName FOR NEXT 30 LINES
	/**
	 * @return true if document was actually persisted
	 */
	public boolean is$persisted() {
		return persisted;
	}
	
	/**
	 * Mark document as persisted or not
	 * @param persisted true means that document is actually present in DB
	 * @return current document
	 */
	public TransponderDocument set$persisted(boolean persisted) {
		this.persisted = persisted;
		return this;
	}
	
	/**
	 * @return name of a collection this document belongs to
	 */
	public String get$collection() {
		return collection;
	}
	
	/**
	 * Sets collection for this document
	 * @param collection name of a collection to set
	 * @return current document
	 */
	public TransponderDocument set$collection(String collection) {
		this.collection = collection;
		return this;
	}

}
