package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.orienteer.transponder.IPolyglot;


/**
 * Annotation to mark methods which should be implemented as lookuping of an entity in a data-source.
 * Corresponding methods might return boolean or a type of an owner of a method.
 * In case of boolean:
 * <ul>
 * <li><b>true</b> - if lookup was successful and current wrapper represent newly found instance</li>
 * <li><b>false</b> - if lookup failed. Current wrapper still holds previous instance</li>
 * </ul>
 * In case of a owner type:
 * <ul>
 * <li><b>current wrapper instance</b> - if lookup was successful and current wrapper represent newly found instance</li>
 * <li><b>null</b> - if lookup failed. Current wrapper still holds previous instance</li>
 * </ul>
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Lookup {
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
