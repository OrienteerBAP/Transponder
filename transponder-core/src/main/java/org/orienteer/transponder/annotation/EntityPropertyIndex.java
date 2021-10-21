package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Property level annotation of an index. Used only for indexes which includes only current property.
 * If more complex indexes are needed: please check {@link EntityIndex}
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface EntityPropertyIndex {
	/**
	 * @return Name of the index
	 */
	public String name() default "";
	/**
	 * @return Type of the index. Might depends on a particular driver
	 */
	public String type() default "";
}
