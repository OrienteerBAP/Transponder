package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;

import static net.bytebuddy.asm.Advice.*;
import static org.orienteer.transponder.CommonUtils.*;

/**
 * Annotation which allow to override returned value if it's null by some default value
 */
@Retention(RUNTIME)
@Target(METHOD)
@AdviceAnnotation(DefaultValue.DefaultValueDelegate.class)
public @interface DefaultValue {
	/**
	 * @return String representation of a default value.
	 * It will be converted into required type automatically.
	 */
	String value();
	
	/**
	 * {@link Advice} delegate which provide required functionality: replace returned object 
	 * by default value if it's null
	 */
	public static class DefaultValueDelegate {
		
		/**
		 * Implementation of the {@link Advice}
		 * @param ret object which was returned by actual method call
		 * @param origin method object to be used for obtaining return type to convert default value to
		 */
		@RuntimeType
		@OnMethodExit
		public static void onExit(@Return(readOnly = false, typing = Typing.DYNAMIC) Object ret, @Origin Method origin) {
			if(ret==null) {
				DefaultValue annotation = origin.getAnnotation(DefaultValue.class);
				ret = stringToInstance(annotation.value(), origin.getReturnType());
			}
		}
		
	}
}
