package org.orienteer.transponder.orientdb;

import org.orienteer.transponder.annotation.EntityType;

@EntityType("DAOTestClassB")
public interface IDAOTestClassB {
	public String getAlias();
	
	public IDAOTestClassA getLinkToA();
	
	public IDAOTestParametrized<IDAOTestClassA> getParameterizedLink();
}
