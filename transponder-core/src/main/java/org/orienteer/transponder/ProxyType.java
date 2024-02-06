package org.orienteer.transponder;

import org.orienteer.transponder.Transponder.ITransponderDelegator;
import org.orienteer.transponder.Transponder.ITransponderEntity;
import org.orienteer.transponder.Transponder.ITransponderHolder;
import org.orienteer.transponder.mutator.StackedMutator;

/**
 * ByteBuddy proxy type to use
 */
public enum ProxyType {
	ENTITY("entity", ITransponderEntity.class, StackedMutator.ENTITY_MUTATOR), 
	DAO("dao", ITransponderHolder.class, StackedMutator.DAO_MUTATOR),
	DELEGATE("delegate", ITransponderDelegator.class, StackedMutator.DELEGATE_MUTATOR);
	
	private final String packageSuffix;
	private final Class<?> transponderInterfaceToImplement;
	private final IMutator rootMutator;
	
	
	ProxyType(String packageSuffix, Class<?> transponderInterfaceToImplement, IMutator rootMutator) {
		this.packageSuffix = packageSuffix;
		this.transponderInterfaceToImplement = transponderInterfaceToImplement;
		this.rootMutator = rootMutator;
	}
	
	/**
	 * Provides default suffix for ByteBuddy generated classes
	 * @return default suffix for ByteBuddy generated classes
	 */
	public String getDefaultPackageSuffix() {
		return packageSuffix;
	}
	
	/**
	 * Provides required Transponder Interface to be implemented
	 * @return required Transponder Interface to be implemented
	 */
	public Class<?> getTransponderInterfaceToImplement() {
		return transponderInterfaceToImplement;
	}
	
	/**
	 * Provides Root Mutator for current proxy type
	 * @return Root Mutator for current proxy type
	 */
	public IMutator getRootMutator() {
		return rootMutator;
	}
}
