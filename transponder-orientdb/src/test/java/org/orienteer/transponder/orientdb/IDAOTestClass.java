package org.orienteer.transponder.orientdb;

import java.util.List;
import java.util.Map;

import org.orienteer.transponder.annotation.AdviceAnnotation;
import org.orienteer.transponder.annotation.DefaultValue;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.annotation.Lookup;
import org.orienteer.transponder.annotation.Query;

import com.orientechnologies.orient.core.record.impl.ODocument;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.Advice.*;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

@EntityType("DAOTestClass")
public interface IDAOTestClass extends IODocumentWrapper {
	public String getName();
	public List<IDAOTestClass> getChild();
	public IDAOTestClass setChild(List<IDAOTestClass> childs);
	
	@EntityProperty("name")
	public String getNameSynonymMethod();
	
	public Map<String, IDAOTestClass> getLinkMap();
	public IDAOTestClass setLinkMap(Map<String, IDAOTestClass> map);
	
	@EntityProperty(value = "linkMap", referencedType = "DAOTestClass")
	public Map<String, ODocument> getLinkMapAsDocuments();
	public IDAOTestClass setLinkMapAsDocuments(Map<String, ODocument> val);
	
	@EntityProperty(value = "child", referencedType = "DAOTestClass")
	public List<ODocument> getChildAsDocuments();
	public IDAOTestClass setChildAsDocuments(List<ODocument> val);
	
	@OrientDBProperty(defaultValue = "true")
	public boolean isPrimitiveSupported();
	public void setPrimitiveSupported(boolean value);
	
	
	default public String getTestName() {
		return "test"+getName();
	}
	
	default public String getTest2Name() {
		return "test2"+getDocument().field("name");
	}
	
	default public String getTest3Name() {
		return "test3"+getTestName();
	}
	
	@Lookup("select from DAOTestClass where name = :name")
//	@DAOHandler(LogMethodHandler.class)
	public boolean lookupToBoolean(String name);
	
	@Lookup("select from DAOTestClass where name = :name")
	public IDAOTestClass lookupAsChain(String name);
	
	@Query("select expand(child) from DAOTestClass where @rid = :target")
	public List<ODocument> listAllChild();
	
	@AdviceAnnotation(TestDAOMethodHandler.class)
	public default Integer interceptedInvocation() {
		return 0;
	}
	
	@DefaultValue("-100")
	public default Integer returnDefaultValue() {
		return null;
	}
	
	public static class TestDAOMethodHandler {
		public static final Integer RETURN = Integer.valueOf(9999);
		
		@Advice.OnMethodExit
		@RuntimeType
		public static void onExit(@Advice.Return(readOnly = false) Integer ret) {
			ret = RETURN;
		}
	}
	
}
