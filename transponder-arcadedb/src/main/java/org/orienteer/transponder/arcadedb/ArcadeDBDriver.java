package org.orienteer.transponder.arcadedb;

import static org.orienteer.transponder.CommonUtils.*;
import static org.orienteer.transponder.arcadedb.ArcadeDBUtils.*;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.orienteer.transponder.CommonUtils;
import org.orienteer.transponder.IDriver;

import com.arcadedb.database.Database;
import com.arcadedb.database.Identifiable;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema;
import com.arcadedb.schema.Type;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Transponder {@link IDriver} for ArcadeDB
 */
public class ArcadeDBDriver implements IDriver {
	
	public static final String DIALECT_ARCADEDB = "arcadedb";
	
	public static final String TYPE_CUSTOM_TRANSPONDER_WRAPPER = "transponder.wrapper";
	
	public static final String INDEX_LSM_TREE_UNIQUE = "LSM_TREE_UNIQUE";
	public static final String INDEX_FULL_TEXT_UNIQUE = "FULL_TEXT_UNIQUE";
	public static final String INDEX_LSM_TREE_NONUNIQUE = "LSM_TREE_NONUNIQUE";
	public static final String INDEX_FULL_TEXT_NONUNIQUE = "FULL_TEXT_NONUNIQUE";
	
	private static final BiMap<String, Class<?>> TYPE_TO_MAIN_CLASS = HashBiMap.create(); 
	
	private final Database database;
	private final boolean overrideSchema;
	
	/**
	 * Creates {@link IDriver} which associated with provided ArcadeDb database
	 * @param database ArcadeDB database instance to associate driver with
	 */
	public ArcadeDBDriver(Database database) {
		this(database, false);
	}
	
	/**
	 * Creates {@link IDriver} which associated with provided ArcadeDb database
	 * @param database ArcadeDB database instance to associate driver with
	 * @param overrideSchema flag which shows should schema be overridden or not
	 */
	public ArcadeDBDriver(Database database, boolean overrideSchema) {
		this.database = database;
		this.overrideSchema = overrideSchema;
	}

	@Override
	public void createType(String typeName, boolean isAbstract, Class<?> mainWrapperClass, String... superTypes) {
		Schema schema = getSchema();
		if(superTypes==null) superTypes = new String[0];
		DocumentType[] parentTypes = new DocumentType[superTypes.length];
		for (int i = 0; i < superTypes.length; i++) {
			String superType = superTypes[i];
			parentTypes[i] = schema.getType(superType);
		} 
		DocumentType type;
		boolean existing = schema.existsType(typeName);
		if(!existing) {
			type = schema.createDocumentType(typeName);
		} else {
			type = schema.getType(typeName);
		}
		if(!existing || overrideSchema) {
			type.setParentTypes(Arrays.asList(parentTypes));
		}
		TYPE_TO_MAIN_CLASS.put(typeName, mainWrapperClass);
	}

	@Override
	public void createProperty(String typeName, String propertyName, java.lang.reflect.Type propertyType, String referencedType,
			int order, AnnotatedElement annotations) {
		ArcadeDBProperty annotation = annotations.getAnnotation(ArcadeDBProperty.class);
		
		if(annotation!=null) referencedType = defaultIfNullOrEmpty(annotation.referencedType(), referencedType);
		
		Schema schema = getSchema();
		DocumentType oClass = schema.getType(typeName);
		Class<?> masterClass = typeToMasterClass(propertyType);
		Class<?> requiredClass = typeToRequiredClass(propertyType);
		if(masterClass.equals(requiredClass)) requiredClass=null;
		Type propType = Type.getTypeByClass(masterClass);
		
		if(propType==null && referencedType!=null) {
			propType = annotation!=null && annotation.embedded() ? Type.EMBEDDED : Type.LINK;
		}
		if(!oClass.existsPolymorphicProperty(propertyName)) {
			oClass.createProperty(propertyName, propType);
		}
		
	}

	@Override
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name) {
		//NOP
	}

	@Override
	public void createIndex(String typeName, String indexName, String indexType, AnnotatedElement annotations,
			String... properties) {
		Schema schema = getSchema();
		DocumentType type = schema.getType(typeName);
		if(type.getPolymorphicIndexByProperties(properties)==null) {
			type.createTypeIndex(indexNameToIndexType(indexType, Schema.INDEX_TYPE.LSM_TREE), 
									isIndexUnique(indexType), properties);
		}
	}

	@Override
	public Object getPropertyValue(Object wrapper, String property) {
		return ((DocumentWrapper)wrapper).get(property);
	}

	@Override
	public void setPropertyValue(Object wrapper, String property, Object value) {
		((DocumentWrapper)wrapper).set(property, value);
	}

	@Override
	public <T> T newEntityInstance(Class<T> proxyClass, String type) {
		try {
			return proxyClass.getConstructor(Database.class, String.class).newInstance(getDatabase(), type);
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't create new entityInstance for class "+proxyClass+" with DocumentType "+type, e);
		}
	}

	@Override
	public void saveEntityInstance(Object wrapper) {
		((DocumentWrapper)wrapper).save();
		((DocumentWrapper)wrapper).reload();
	}

	@Override
	public <T> T wrapEntityInstance(Class<T> proxyClass, Object seed) {
		try {
			return proxyClass.getConstructor(Identifiable.class).newInstance((Identifiable)seed);
		}catch (Exception e) {
			throw new IllegalArgumentException("Can't wrap seed by class "+proxyClass+". Seed: "+seed, e);
		}
	}

	@Override
	public Class<?> getDefaultEntityBaseClass() {
		return DocumentWrapper.class;
	}

	@Override
	public Class<?> getEntityMainClass(Object seed) {
		return TYPE_TO_MAIN_CLASS.get(((Identifiable)seed).asDocument().getTypeName());
	}

	@Override
	public boolean isSeedClass(Class<?> seedClass) {
		return Identifiable.class.isAssignableFrom(seedClass);
	}

	@Override
	public Object toSeed(Object wrapped) {
		return ((DocumentWrapper)wrapped).getDocument();
	}

	@Override
	public List<Object> query(String language, String query, Map<String, Object> params) {
		try(ResultSet rs = database.query(CommonUtils.defaultIfNullOrEmpty(language, "sql"), query, params)) {
			return rs.stream().map(r -> r.getElement().orElse(null)).collect(Collectors.toList());
		}
	}
	
	@Override
	public Object querySingle(String language, String query, Map<String, Object> params) {
		try(ResultSet rs = database.query(CommonUtils.defaultIfNullOrEmpty(language, "sql"), query, params)) {
			return rs.stream().map(r -> r.getElement().orElse(null)).findFirst().orElse(null);
		}
	}
	
	@Override
	public Object command(String language, String command, Map<String, Object> params) {
		try(ResultSet rs = database.command(CommonUtils.defaultIfNullOrEmpty(language, "sql"), command, params)) {
			List<?> ret = rs.stream().map(r -> r.getElement().orElse(null)).collect(Collectors.toList());
			return ret.isEmpty()?null:(ret.size()==1?ret.get(0):ret);
		}
	}
	
	@Override
	public void replaceSeed(Object wrapper, Object newSeed) {
		((DocumentWrapper)wrapper).setDocument((Identifiable)newSeed);
	}
	
	@Override
	public String getDialect() {
		return DIALECT_ARCADEDB;
	}

	/**
	 * @return {@link Schema} for the associated ArcadeDB database
	 */
	public Schema getSchema() {
		return getDatabase().getSchema();
	}
	
	/**
	 * @return associated ArcadeDB instance
	 */
	public Database getDatabase() {
		return database;
	}
	
}
