package org.orienteer.transponder.orientdb;


public interface IDAOChild extends IDAOParent {

	@Override
	public default int methodWithNoBodyInParent() {
		return -1;
	}
	
	public default Class<?> methodWithDefaultBodyInParent() {
		return IDAOChild.class;
	}
	
	@Override
	public default void methodVoidWithException() {
		
	}
	
	public default void methodVoid() {
		throw new IllegalStateException("This is OK");
	}
}
