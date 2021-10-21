package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;

/**
 * Annotation to mark other annotations which generates byte-code {@link MethodDelegation}
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface DelegateAnnotation {
	/**
	 * @return Class to be used as delegate for {@link MethodDelegation}
	 */
	Class<?> value();
}
