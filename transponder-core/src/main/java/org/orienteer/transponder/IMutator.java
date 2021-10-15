package org.orienteer.transponder;

import net.bytebuddy.dynamic.DynamicType;

public interface IMutator {
	public default <T> DynamicType.Builder<T> mutate(Transponder transponder, 
													 DynamicType.Builder<T> builder,
													 BuilderScheduler scheduler){
		schedule(transponder, scheduler);
		return builder;
	}
	
	public default void schedule(Transponder transponder, BuilderScheduler scheduler) {
		
	}
}
