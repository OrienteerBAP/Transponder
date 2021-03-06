package org.orienteer.transponder;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;

/**
 * Interface for mutators: objects which can manipulate by {@link DynamicType.Builder} 
 */
public interface IMutator {
	/**
	 * Mutates provided builder
	 * @param <T> type of an class to be generated by ByteBuddy
	 * @param transponder transponder instance for which class is being generated
	 * @param builder ByteBuddy builder
	 * @param scheduler instance of a scheduler which simplifies declarative way for
	 *  defining {@link Advice} and {@link MethodDelegation} 
	 * @return mutated builder
	 */
	public default <T> DynamicType.Builder<T> mutate(Transponder transponder, 
													 DynamicType.Builder<T> builder,
													 BuilderScheduler scheduler){
		schedule(transponder, scheduler);
		return builder;
	}
	
	/**
	 * More specific method to be used just for scheduling
	 * @param transponder transponder instance for which class is being generated
	 * @param scheduler instance of a scheduler which simplifies declarative way for
	 *  defining {@link Advice} and {@link MethodDelegation} 
	 */
	public default void schedule(Transponder transponder, BuilderScheduler scheduler) {
		
	}
}
