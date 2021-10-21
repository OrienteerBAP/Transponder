package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.bytebuddy.asm.Advice;

/**
 * Annotation to mark other annotations which generates byte-code {@link Advice}.
 * For example: {@link DefaultValue}
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface AdviceAnnotation {
	/**
	 * @return Class to be used as delegate for {@link Advice} creation.
	 */
	Class<?> value();
}
