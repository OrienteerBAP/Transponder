package org.orienteer.transponder.orientdb;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.emptyToNull;
import static org.orienteer.transponder.CommonUtils.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

import org.orienteer.transponder.IDriver;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;

import com.google.common.base.Strings;
import com.google.common.primitives.Primitives;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

public class ODriver implements IDriver {
	
	public static final String OCLASS_CUSTOM_TRANSPONDER_WRAPPER = "transponder.wrapper";
	
	public static final String OINDEX_UNIQUE = "UNIQUE";
	public static final String OINDEX_NOTUNIQUE = "NOTUNIQUE";
	public static final String OINDEX_FULLTEXT = "FULLTEXT";
	public static final String OINDEX_DICTIONARY = "DICTIONARY";
	public static final String OINDEX_PROXY = "PROXY";
	public static final String OINDEX_UNIQUE_HASH_INDEX = "UNIQUE_HASH_INDEX";
	public static final String OINDEX_NOTUNIQUE_HASH_INDEX = "NOTUNIQUE_HASH_INDEX";
	public static final String OINDEX_DICTIONARY_HASH_INDEX = "DICTIONARY_HASH_INDEX";
	public static final String OINDEX_SPATIAL = "SPATIAL";
	
	private static final Map<OType, OType> EMBEDDED_TO_LINKS_MAP = toMap(OType.EMBEDDED, OType.LINK,
																		 OType.EMBEDDEDLIST, OType.LINKLIST,
																		 OType.EMBEDDEDSET, OType.LINKSET,
																		 OType.EMBEDDEDMAP, OType.LINKMAP);
	
	private final boolean overrideSchema;
	
	public ODriver() {
		this(false);
	}
	
	public ODriver(boolean overrideSchema) {
		this.overrideSchema = overrideSchema;
	}

	@Override
	public void createType(String typeName, boolean isAbstract, Class<?> mainWrapperClass, String... superTypes) {
		OSchema schema = getSchema();
		if(superTypes==null) superTypes = new String[0];
		OClass[] superClasses = new OClass[superTypes.length];
		for (int i = 0; i < superTypes.length; i++) {
			String superType = superTypes[i];
			superClasses[i] = schema.getClass(superType);
		}
		OClass oClass = schema.getClass(typeName);
		boolean existing = oClass!=null;
		if(!existing) {
			if(isAbstract) {
				oClass = schema.createAbstractClass(typeName, superClasses);
			} else {
				oClass = schema.createClass(typeName, superClasses);
			}
		} else if(overrideSchema) {
			oClass.setSuperClasses(Arrays.asList(superClasses));
			if(isAbstract!=oClass.isAbstract())oClass.setAbstract(isAbstract);
		}
		if(!existing || overrideSchema) {
			String currentValue = oClass.getCustom(OCLASS_CUSTOM_TRANSPONDER_WRAPPER);
			String transponderWrapperName = mainWrapperClass.getName();
			if(!Objects.equals(currentValue, transponderWrapperName)) {
				oClass.setCustom(OCLASS_CUSTOM_TRANSPONDER_WRAPPER, transponderWrapperName);
			}
		}
	}

	@Override
	public void createProperty(String typeName, String propertyName, Type propertyType, String linkedClassName, int order, AnnotatedElement annotations) {
		OrientDBProperty annotation = annotations.getAnnotation(OrientDBProperty.class);
		
		if(annotation!=null) linkedClassName = defaultIfNullOrEmpty(annotation.linkedClass(), linkedClassName);
		
		OSchema schema = getSchema();
		OClass oClass = schema.getClass(typeName);
		Class<?> masterClass = typeToMasterClass(propertyType);
		Class<?> requiredClass = typeToRequiredClass(propertyType);
		if(masterClass.equals(requiredClass)) requiredClass=null;
		OType type = getTypeByClass(masterClass);
		if(annotation!=null && !OType.ANY.equals(annotation.type())) type = annotation.type();
		OType linkedType = requiredClass==null || linkedClassName!=null? null : getTypeByClass(requiredClass);
		if(annotation!=null && !OType.ANY.equals(annotation.linkedType())) linkedType = annotation.linkedType();
		
		if(type==null && linkedClassName!=null) {
			type = OType.EMBEDDED;
		}
		if(linkedClassName!=null && EMBEDDED_TO_LINKS_MAP.containsKey(type) &&(annotation==null || !annotation.embedded())) {
			type = EMBEDDED_TO_LINKS_MAP.get(type);
		}
		if(type==null) type=OType.ANY;
		OProperty property = oClass.getProperty(propertyName);
		boolean justCreated = property==null;
		OClass linkedClass = linkedClassName!=null?schema.getClass(linkedClassName):null; //Might be null even if linkedClassName is not
		if(justCreated) {
			property = linkedClassName!=null?oClass.createProperty(propertyName, type, linkedClass)
										:oClass.createProperty(propertyName, type, linkedType);
		} else if (overrideSchema){
			if(!Objects.equals(property.getType(), type)) property.setType(type);
			if(!Objects.equals(property.getLinkedType(), linkedType)) property.setLinkedType(linkedType);
			if(!Objects.equals(property.getLinkedClass(), linkedClass)
					&& ((linkedClass!=null && linkedClassName!=null)
						|| (linkedClass==null && linkedClassName==null))) property.setLinkedClass(linkedClass);
		}
		boolean shouldBeNotNull = propertyType instanceof Class && ((Class<?>)propertyType).isPrimitive();
		if(annotation!=null && (justCreated || overrideSchema)) {
			if(!Objects.equals(property.isNotNull(), shouldBeNotNull | annotation.notNull())) property.setNotNull(shouldBeNotNull | annotation.notNull());
			if(!Objects.equals(property.isMandatory(), annotation.mandatory())) property.setMandatory(annotation.mandatory());
			if(!Objects.equals(property.isReadonly(), annotation.readOnly())) property.setReadonly(annotation.readOnly());
			if(!Objects.equals(property.getMin(), emptyToNull(annotation.min())))
										property.setMin(emptyToNull(annotation.min()));
			if(!Objects.equals(property.getMax(), emptyToNull(annotation.max())))
										property.setMax(annotation.max());
			if(!Objects.equals(property.getRegexp(), emptyToNull(annotation.regexp())))
										property.setRegexp(emptyToNull(annotation.regexp()));
			if(!Objects.equals(property.getCollate().getName(), annotation.collate())) property.setCollate(annotation.collate());
			if(!Objects.equals(property.getDefaultValue(), emptyToNull(annotation.defaultValue())))
										property.setDefaultValue(emptyToNull(annotation.defaultValue()));
		}
		if(annotation==null)
			if(!Objects.equals(property.isNotNull(), shouldBeNotNull | property.isNotNull())) property.setNotNull(shouldBeNotNull | property.isNotNull());
		
	}
	
	@Override
	public void createIndex(String typeName, String indexName, String indexType, AnnotatedElement annotations,
			String... properties) {
		OSchema schema = getSchema();
		OClass oClass = schema.getClass(typeName);
		if(oClass.getClassIndex(indexName)==null)
			oClass.createIndex(indexName, indexType, properties);
	}

	@Override
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name) {
		OSchema schema = getSchema();
		OClass class1 = schema.getClass(type1Name);
		OProperty property1 = class1.getProperty(property1Name);
		OClass class2 = schema.getClass(type2Name);
		OProperty property2 = property2Name!=null?class1.getProperty(property2Name):null;
		if(!Objects.equals(property1.getLinkedClass(), class2)) property1.setLinkedClass(class2);
		if(property2!=null 
				&& !Objects.equals(property2.getLinkedClass(), class1)) property2.setLinkedClass(class1);
	}

	@Override
	public Object getPropertyValue(Object wrapper, String property) {
		if(wrapper==null) return null;
		ODocument doc = ((ODocumentWrapper)wrapper).getDocument();
		return doc!=null?doc.field(property):null;
	}

	@Override
	public void setPropertyValue(Object wrapper, String property, Object value) {
		if(wrapper==null) return;
		ODocument doc = ((ODocumentWrapper)wrapper).getDocument();
		if(doc!=null) doc.field(property, value);
	}

	@Override
	public <T> T newEntityInstance(Class<T> proxyClass, String type) {
		try {
			ODocument doc = new ODocument(type);
			return proxyClass.getConstructor(ODocument.class).newInstance(doc);
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't create new entityInstance for class "+proxyClass+" with OClass "+type, e);
		} 
	}
	
	@Override
	public void saveEntityInstance(Object wrapper) {
		asWrapper(wrapper).save();
	}

	@Override
	public <T> T wrapEntityInstance(Class<T> proxyClass, Object seed) {
		try {
			ODocument doc = ((OIdentifiable)seed).getRecord();
			return proxyClass.getConstructor(ODocument.class).newInstance(doc);
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't wrap seed by class "+proxyClass+". Seed: "+seed, e);
		}
	}

	@Override
	public Class<?> getDefaultEntityBaseClass() {
		return ODocumentWrapper.class;
	}

	@Override
	public Class<?> getEntityMainClass(Object seed) {
		if(seed==null) return null;
		ODocument doc = ((OIdentifiable)seed).getRecord();
		if(doc!=null) {
			String mainWrapperClassName = doc.getSchemaClass().getCustom(OCLASS_CUSTOM_TRANSPONDER_WRAPPER);
			if(!Strings.isNullOrEmpty(mainWrapperClassName)) {
				try {
					return Class.forName(mainWrapperClassName);
				} catch (ClassNotFoundException e) {
					//NOP
				}
			}
		}
		
		return null;
	}

	@Override
	public boolean isSeedClass(Class<?> seedClass) {
		return OIdentifiable.class.isAssignableFrom(seedClass);
	}

	@Override
	public Object toSeed(Object wrapped) {
		if(wrapped==null) return null;
		return ((ODocumentWrapper)wrapped).getDocument();
	}

	@Override
	public List<Object> query(String language, String query, Map<String, Object> params) {
		try(OResultSet resultSet = getSession().query(query, params)) {
			return resultSet.elementStream().map(e -> {
						ODocument doc = (ODocument)e;
						if(doc.getClassName()!=null) return doc;
						else return doc.field("value");
					}).collect(Collectors.toList());
		}
	}
	
	@Override
	public void replaceSeed(Object wrapper, Object newSeed) {
		((ODocumentWrapper)wrapper).fromStream(((OIdentifiable)newSeed).getRecord());
	}
	
	protected ODatabaseSession getSession() {
		ODatabaseSession db = ODatabaseRecordThreadLocal.instance().get();
		if(db==null) throw new IllegalStateException("OrientDB Session is not associated with current thread");
		return db;
	}
	
	protected OSchema getSchema() {
		return getSession().getMetadata().getSchema();
	}
	
	private OType getTypeByClass(Class<?> clazz) {
		OType ret = OType.getTypeByClass(clazz);
		if(OType.CUSTOM.equals(ret) && Serializable.class.isAssignableFrom(clazz)) ret = null;
		return ret;
	}
	
	public static ODocumentWrapper asWrapper(Object obj) {
		if(obj==null) return null;
		else if (obj instanceof ODocumentWrapper) return (ODocumentWrapper)obj;
		else throw new IllegalStateException("Object is not a wrapper. Object: "+obj);
	}
	
	public static ODocument asDocument(Object obj) {
		return obj!=null?asWrapper(obj).getDocument():null;
	}
	
	public static <T> T save(T obj) {
		asWrapper(obj).save();
		return obj;
	}
	
	public static <T> T reload(T obj) {
		asWrapper(obj).reload();
		return obj;
	}
}
