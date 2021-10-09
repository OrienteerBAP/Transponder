package org.orienteer.transponder.orientdb;

import com.orientechnologies.orient.core.db.record.ridbag.ORidBag;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.orienteer.transponder.annotation.EntityType;

@EntityType("DAOAllTypesTestClass")
public interface IDAOAllTypesTestClass {

	@OrientDBProperty(linkedClass = "IDAODummyClass", type = OType.LINKLIST)
	public List<ODocument> getDocs();
	public void setDocs(List<ODocument> val);

	  /**
	   * BOOLEAN("Boolean", 0, Boolean.class, new Class<?>[] { Number.class }),
	   */
	public Boolean getBoolean();
	public void setBoolean(Boolean val);
	
	public boolean getBooleanPrimitive();
	public void setBooleanPrimitive(boolean val);
	
	@OrientDBProperty(notNull = true)
	public Boolean getBooleanDeclared();
	public void setBooleanDeclared(Boolean val);

	/**
	 * INTEGER("Integer", 1, Integer.class, new Class<?>[] { Number.class }),
	 */
	
	public Integer getInteger();
	public void setInteger(Integer val);

	/**
	 * SHORT("Short", 2, Short.class, new Class<?>[] { Number.class }),
	 */
	
	public Short getShort();
	public void setShort(Short val); 

	/**
	 * LONG("Long", 3, Long.class, new Class<?>[] { Number.class, }),
	 */
	
	public Long getLong();
	public void setLong(Long val);

	/**
	 * FLOAT("Float", 4, Float.class, new Class<?>[] { Number.class }),
	 */
	
	public Float getFloat();
	public void setFloat(Float val);

	/**
	 * DOUBLE("Double", 5, Double.class, new Class<?>[] { Number.class }),
	 */
	
	public Double getDouble();
	public void setDouble(Double val);

	/**
	 * DATETIME("Datetime", 6, Date.class, new Class<?>[] { Date.class, Number.class }),
	 */
	
	public Date getDateTime();
	public void setDateTime(Date val);

	/**
	 * STRING("String", 7, String.class, new Class<?>[] { Enum.class }),
	 */
	
	public String getString();
	public void setString(String val);

	/**
	 * BINARY("Binary", 8, byte[].class, new Class<?>[] { byte[].class }),
	 */
	
	public byte[] getBinary();
	public void setBinary(byte[] val);

	/**
	 * EMBEDDED("Embedded", 9, Object.class, new Class<?>[] { ODocumentSerializable.class, OSerializableStream.class }),
	 */
	@OrientDBProperty(embedded = true)
	public IDAODummyClass getEmbedded();
	public void setEmbedded(IDAODummyClass val);

	/**
	 * EMBEDDEDLIST("EmbeddedList", 10, List.class, new Class<?>[] { List.class, OMultiCollectionIterator.class }),
	 */
	@OrientDBProperty(embedded = true)
	public List<IDAODummyClass> getEmbeddedList();
	public void setEmbeddedList(List<IDAODummyClass> val);
	
	public List<String> getEmbeddedStringList();
	public void setEmbeddedStringList(List<String> val);

	/**
	 * EMBEDDEDSET("EmbeddedSet", 11, Set.class, new Class<?>[] { Set.class }),
	 */

	@OrientDBProperty(embedded = true)
	public Set<IDAODummyClass> getEmbeddedSet();
	public void setEmbeddedSet(Set<IDAODummyClass> val);
	
	public Set<String> getEmbeddedStringSet();
	public void setEmbeddedStringSet(Set<String> val);
	/**
	 * EMBEDDEDMAP("EmbeddedMap", 12, Map.class, new Class<?>[] { Map.class }),
	 */
	@OrientDBProperty(embedded = true)
	public Map<String, IDAODummyClass> getEmbeddedMap();
	public void setEmbeddedMap(Map<String, IDAODummyClass> val);
	
	public Map<String, String> getEmbeddedStringMap();
	public void setEmbeddedStringMap(Map<String, String> val);

	/**
	 * LINK("Link", 13, OIdentifiable.class, new Class<?>[] { OIdentifiable.class, ORID.class }),
	 */
	
	public IDAODummyClass getLink();
	public void setLink(IDAODummyClass val);

	/**
	 * LINKLIST("LinkList", 14, List.class, new Class<?>[] { List.class }),
	 */
	public List<IDAODummyClass> getLinkList();
	public void setLinkList(List<IDAODummyClass> val);



	/**
	 * LINKSET("LinkSet", 15, Set.class, new Class<?>[] { Set.class }),
	 */
	public Set<IDAODummyClass> getLinkSet();
	public void setLinkSet(Set<IDAODummyClass> val);

	/**
	 * LINKMAP("LinkMap", 16, Map.class, new Class<?>[] { Map.class }),
	 */
	
	public Map<String, IDAODummyClass> getLinkMap();
	public void setLinkMap(Map<String, IDAODummyClass> val);

	/**
	 * BYTE("Byte", 17, Byte.class, new Class<?>[] { Number.class }),
	 */
	
	public Byte getByte();
	public void setByte(Byte val);

	/**
	 * TRANSIENT("Transient", 18, null, new Class<?>[] {}),
	 */
	
	@OrientDBProperty(type = OType.TRANSIENT)
	public Object getTransient();
	public void setTransient(Object val);

	/**
	 * DATE("Date", 19, Date.class, new Class<?>[] { Number.class }),
	 */
	
	@OrientDBProperty(type = OType.DATE)
	public Date getDate();
	public void setDate(Date val);

	/**
	 * CUSTOM("Custom", 20, OSerializableStream.class, new Class<?>[] { OSerializableStream.class, Serializable.class }),
	 */
	@OrientDBProperty(type = OType.CUSTOM)
	public Serializable getCustom();
	public void setCustom(Serializable val);

	/**
	 * DECIMAL("Decimal", 21, BigDecimal.class, new Class<?>[] { BigDecimal.class, Number.class }),
	 */
	
	public BigDecimal getDecimal();
	public void setDecimal(BigDecimal val);

	/**
	 * LINKBAG("LinkBag", 22, ORidBag.class, new Class<?>[] { ORidBag.class }),
	 */
	
	@OrientDBProperty(linkedClass = "IDAODummyClass")
	public ORidBag getLinkBag();
	public void setLinkBag(ORidBag val);

	/**
	 * ANY("Any", 23, null, new Class<?>[] {});
	 */
	@OrientDBProperty(type = OType.ANY)
	public Object getAny();
	public void setAny(Object val);
	
	public DayOfWeek getEnum();
	public void setEnum(DayOfWeek value);
}
