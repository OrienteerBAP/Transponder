package org.orienteer.transponder.neo4j;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotations to mark classes which actually should be mapped to relationships
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Neo4JRelationship {

}
