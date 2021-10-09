package org.orienteer.transponder.orientdb;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.emptyToNull;
import static org.orienteer.transponder.CommonUtils.*;

import org.orienteer.transponder.IDriver;

import com.google.common.base.Strings;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

public class ODriver implements IDriver {
	
	public static final String OCLASS_CUSTOM_TRANSPONDER_WRAPPER = "transponder.wrapper";
	
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
	public void createProperty(String typeName, String propertyName, Type propertyType, String referencedType, int order, AnnotatedElement annotations) {
		OrientDBProperty annotation = annotations.getAnnotation(OrientDBProperty.class);
		
		if(annotation!=null) referencedType = defaultIfNullOrEmpty(annotation.linkedClass(), referencedType);
		
		OSchema schema = getSchema();
		OClass oClass = schema.getClass(typeName);
		OClass linkedClass = referencedType!=null?schema.getClass(referencedType):null;
		Class<?> masterClass = typeToMasterClass(propertyType);
		Class<?> requiredClass = typeToRequiredClass(propertyType, null);
		if(masterClass.equals(requiredClass)) requiredClass=null;
		OType type = getTypeByClass(masterClass);
		if(annotation!=null && !OType.ANY.equals(annotation.type())) type = annotation.type();
		OType linkedType = requiredClass==null || linkedClass!=null? null : getTypeByClass(requiredClass);
		if(annotation!=null && !OType.ANY.equals(annotation.linkedType())) linkedType = annotation.linkedType();
		
		if(type==null && linkedClass!=null) {
			type = OType.EMBEDDED;
		}
		if(linkedClass!=null && EMBEDDED_TO_LINKS_MAP.containsKey(type) &&(annotation==null || !annotation.embedded())) {
			type = EMBEDDED_TO_LINKS_MAP.get(type);
		}
		
		OProperty property = oClass.getProperty(propertyName);
		boolean justCreated = property==null;
		if(justCreated) {
			property = linkedClass!=null?oClass.createProperty(propertyName, type, linkedClass)
										:oClass.createProperty(propertyName, type, linkedType);
		} else if (overrideSchema){
			if(!Objects.equals(property.getType(), type)) property.setType(type);
			if(!Objects.equals(property.getLinkedType(), linkedType)) property.setLinkedType(linkedType);
			if(!Objects.equals(property.getLinkedClass(), linkedClass)) property.setLinkedClass(linkedClass);
		}
		if(annotation!=null && (justCreated || overrideSchema)) {
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
	}

	@Override
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name) {
		
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
