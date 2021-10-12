package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(EntityIndexes.class)
public @interface EntityIndex {
	public String name();
	public String type() default "";
	public String[] properties();
}
