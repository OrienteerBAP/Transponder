package org.orienteer.transponder.arcadedb;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.arcadedb.schema.Type;

/**
 * Annotation to customize property creation within ArcadeDB
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ArcadeDBProperty {
	/**
	 * @return true if {@link Type#EMBEDDED} instead of {@link Type#LINK} 
	 */
	boolean embedded();
}
