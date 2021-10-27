package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Optional annotation to mark getter/setter methods
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface EntityProperty {
	/**
	 * @return name of the property. Can be used to override default property name which derived from method name
	 */
	String value() default "";
	/**
	 * @return type name of a referenced type. Used for properties which store links to other entities
	 */
	String referencedType() default "";
	/**
	 * @return property name which might reference back from entity which is referenced by this property
	 */
	String inverse() default "";
}
