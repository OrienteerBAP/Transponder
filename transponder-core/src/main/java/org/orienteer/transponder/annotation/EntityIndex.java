package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Entity level definition of an index
 */
@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(EntityIndexes.class)
public @interface EntityIndex {
	/**
	 * @return Name of the index
	 */
	public String name();
	/**
	 * @return Type of the index. Might depends on a particular driver
	 */
	public String type() default "";
	/**
	 * @return Set of properties to be included into the index
	 */
	public String[] properties();
}
