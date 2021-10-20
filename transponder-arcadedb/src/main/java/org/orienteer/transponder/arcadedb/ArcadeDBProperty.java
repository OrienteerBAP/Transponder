package org.orienteer.transponder.arcadedb;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface ArcadeDBProperty {
	String referencedType() default "";
	boolean embedded() default false;
}
