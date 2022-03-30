package org.orienteer.transponder;

import org.orienteer.transponder.Transponder.ITransponderEntity;
import org.orienteer.transponder.Transponder.ITransponderHolder;
import org.orienteer.transponder.mutator.StackedMutator;

public enum ProxyType {
	ENTITY("entity", ITransponderEntity.class, StackedMutator.ENTITY_MUTATOR), 
	DAO("dao", ITransponderHolder.class, StackedMutator.DAO_MUTATOR);
	
	private final String packageSuffix;
	private final Class<?> transponderInterfaceToImplement;
	private final IMutator rootMutator;
	
	
	ProxyType(String packageSuffix, Class<?> transponderInterfaceToImplement, IMutator rootMutator) {
		this.packageSuffix = packageSuffix;
		this.transponderInterfaceToImplement = transponderInterfaceToImplement;
		this.rootMutator = rootMutator;
	}
	
	public String getDefaultPackageSuffix() {
		return packageSuffix;
	}
	
	public Class<?> getTransponderInterfaceToImplement() {
		return transponderInterfaceToImplement;
	}
	
	public IMutator getRootMutator() {
		return rootMutator;
	}
}
