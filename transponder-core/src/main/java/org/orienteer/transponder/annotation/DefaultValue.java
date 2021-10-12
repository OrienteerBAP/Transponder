package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;

import static net.bytebuddy.asm.Advice.*;
import static org.orienteer.transponder.CommonUtils.*;

@Retention(RUNTIME)
@Target(METHOD)
@AdviceAnnotation(DefaultValue.DefaultValueDelegate.class)
public @interface DefaultValue {
	
	String value();

	public static class DefaultValueDelegate {
		
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
