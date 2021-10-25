package org.orienteer.transponder.arcadedb;

import com.arcadedb.schema.Schema;

import lombok.experimental.UtilityClass;

/**
 * ArcadeDB specific utility methods
 */
@UtilityClass
public class ArcadeDBUtils {
	
	/**
	 * Converts name of an index type to actual {@link Schema.INDEX_TYPE} enum
	 * @param indexTypeName name of an index type
	 * @param defIndexType default index type
	 * @return {@link Schema.INDEX_TYPE} enum
	 */
	public Schema.INDEX_TYPE indexNameToIndexType(String indexTypeName, Schema.INDEX_TYPE defIndexType) {
		if(indexTypeName!=null) {
			for (Schema.INDEX_TYPE type : Schema.INDEX_TYPE.values()) {
				if(indexTypeName.startsWith(type.name())) return type;
			} 
		}
		return defIndexType;
	}
	
	/**
	 * Check suffix in index type name and return flag about uniqueness
	 * @param indexTypeName name of an index type
	 * @return true of index is for unique values
	 */
	public Boolean isIndexUnique(String indexTypeName) {
		return indexTypeName!=null && indexTypeName.endsWith("_UNIQUE");
	}
}
