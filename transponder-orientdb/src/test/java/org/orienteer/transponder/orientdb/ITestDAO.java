package org.orienteer.transponder.orientdb;

import java.util.List;

import org.orienteer.transponder.annotation.Query;
import org.orienteer.transponder.annotation.common.Sudo;

import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;

public interface ITestDAO {

	@Query("select from DAOTestClass")
	public List<ODocument> listDAOTestClass();
	
	
	@Query("select from DAOTestClass where name = :name")
	public ODocument findSingleAsDocument(String name);
	
	@Query("select from DAOTestClass where name = :name")
	public IDAOTestClass findSingleAsDAO(String name);
	
	@Query("select from DAOTestClass")
	public List<ODocument> findAllAsDocument();
	
	@Query("select from DAOTestClass")
	public List<IDAOTestClass> findAllAsDAO();
	
	@Sudo
	public default String sudoWhoAmI() {
		return whoAmI();
	}
	
	public default String whoAmI() {
		OSecurityUser user = ODatabaseRecordThreadLocal.instance().get().getUser();
		return user!=null?user.getName():null;
	}
	
	@Sudo
	public default OUser createUser() {
		return new OUser("testUser", "testUser").save();
	}
	
	default public int countAll() {
		return listDAOTestClass().size();
	}
}
