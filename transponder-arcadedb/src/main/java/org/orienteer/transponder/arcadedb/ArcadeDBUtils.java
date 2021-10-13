package org.orienteer.transponder.arcadedb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ArcadeDBUtils {
	
	//TODO: Remove once PR https://github.com/ArcadeData/arcadedb/pull/133 merged
	public void setParentTypes(DocumentType type, List<DocumentType> newParents) {
		List<DocumentType> commonParents = new ArrayList<>(type.getParentTypes());
		commonParents.retainAll(newParents);
		List<DocumentType> toRemove = new ArrayList<>(type.getParentTypes());
		toRemove.removeAll(commonParents);
		toRemove.forEach(type::removeParentType);
		List<DocumentType> toAdd = new ArrayList<>(newParents);
		toAdd.removeAll(commonParents);
		toAdd.forEach(type::addParentType);
	}
	
	//TODO: Remove once PR https://github.com/ArcadeData/arcadedb/pull/133 merged
	public Set<String> getPolymorphicPropertyNames(DocumentType type, String propertyName) {
		Set<String> allproperties = new HashSet<>();
		collectPolymorphicPropertyNames(type, propertyName, allproperties);
		return allproperties;
	}
	
	//TODO: Remove once PR https://github.com/ArcadeData/arcadedb/pull/133 merged
	public void collectPolymorphicPropertyNames(DocumentType type, String propertyName, Set<String> set) {
		set.addAll(type.getPropertyNames());
		for (DocumentType parent : type.getParentTypes()) {
			collectPolymorphicPropertyNames(parent, propertyName, set);
		}
	}
	
	//TODO: Remove once PR https://github.com/ArcadeData/arcadedb/pull/133 merged
	public boolean existsPolymorphicProperty(DocumentType type, String propertyName) {
		if(type.getPropertyNames().contains(propertyName)) return true;
		for (DocumentType parent : type.getParentTypes()) {
			if(existsPolymorphicProperty(parent, propertyName)) return true;
		}
		return false;
	}
	
	//TODO: Remove once PR https://github.com/ArcadeData/arcadedb/pull/132 merged
	public Schema.INDEX_TYPE indexNameToIndexType(String indexTypeName, Schema.INDEX_TYPE defIndexType) {
		if(indexTypeName!=null) {
			for (Schema.INDEX_TYPE type : Schema.INDEX_TYPE.values()) {
				if(indexTypeName.startsWith(type.name())) return type;
			} 
		}
		return defIndexType;
	}
	
	public Boolean isIndexUnique(String indexTypeName) {
		return indexTypeName!=null && indexTypeName.endsWith("_UNIQUE");
	}
}
