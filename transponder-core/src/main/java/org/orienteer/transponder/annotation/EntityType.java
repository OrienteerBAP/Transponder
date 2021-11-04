package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to mark interfaces/classes which can be used for wrapping an data-source's entities
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface EntityType {
	/**
	 * @return type name in corresponding data-source
	 */
	String value();
	/**
	 * @return set of type names of super types which should be additionally used during definition.
	 * Transponder automatically adds super types if they were provided through <b>extends</b>
	 */
	String[] superTypes() default {};
	/**
	 * @return flag to denote types which should be defined as abstract
	 */
	boolean isAbstract() default false;
	
	/**
	 * Defines from what order property enumeration should start.
	 * Useful when subclass should continue ordering of properties from the parent.
	 * If driver doesn't support orders - there is no effect. Consult with documentation.
	 * @return integer from which order number for properties being created should start.
	 */
	int orderOffset() default 0;
	
	/**
	 * @return how much increase order for next property
	 * Useful when subinterface doesn't want to change setup of parent class
	 */
	int orderStep() default 10;
}
