package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.orienteer.transponder.IPolyglot;

/**
 * Annotation to mark methods which should be implemented as query execution over a data-source
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Query {
	/**
	 * @return query ID to be used by {@link IPolyglot} for finding actual query to be executed.
	 * In case if it's not default ID will be equal to <b>className.methodName</b>
	 */
	String id() default "";
	/**
	 * @return query to be executed. Might be empty.
	 */
	String value() default "";
	/**
	 * @return language to be used for command execution. Passed as-is to driver. {@link IPolyglot} can override language
	 */
	String language() default "";
	/**
	 * @return dialect of the specified command in {@link #value()}
	 */
	String dialect() default "";
}
