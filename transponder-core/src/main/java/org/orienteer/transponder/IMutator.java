package org.orienteer.transponder;

import net.bytebuddy.dynamic.DynamicType;

public interface IMutator {
	public default <T> DynamicType.Builder<T> mutate(DynamicType.Builder<T> builder, BuilderScheduler scheduler){
		schedule(scheduler);
		return builder;
	}
	
	public default void schedule(BuilderScheduler scheduler) {
		
	}
}
