package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.bytebuddy.asm.Advice;

/**
 * Annotation to mark other annotations which generates byte-code {@link Advice}.
 * For example: {@link DefaultValue}
 */
@Retention(RUNTIME)
@Target({ANNOTATION_TYPE, METHOD})
@Repeatable(AdviceAnnotation.List.class)
public @interface AdviceAnnotation {
	/**
	 * @return Class to be used as delegate for {@link Advice} creation.
	 */
	Class<?> value() default Object.class;
	
	/**
	 * Annotation to collect repeatable {@link AdviceAnnotation}s
	 */
	@Retention(RUNTIME)
	@Target({ ANNOTATION_TYPE, METHOD })
	@interface List {
		/**
		 * @return array of defined {@link AdviceAnnotation}s
		 */
		AdviceAnnotation[] value();
	}
}
