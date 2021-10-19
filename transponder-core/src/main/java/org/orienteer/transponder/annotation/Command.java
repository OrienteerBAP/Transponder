package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Annotation for methods executes SQL commands in the DB
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Command {
	String id() default "";
	String value() default "";
	String language() default "";
	String dialect() default "";
}
