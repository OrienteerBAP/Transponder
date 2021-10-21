package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for repeatable {@link EntityIndex}
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface EntityIndexes {
	/**
	 * @return set of {@link EntityIndex}
	 */
	EntityIndex[] value();
}
