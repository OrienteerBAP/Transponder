package org.orienteer.transponder.annotation.common;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.orienteer.transponder.annotation.AdviceAnnotation;

/**
 * Execute annotated method under privileged rights.
 * Please check used driver for supporting this annotation.
 */
@Retention(RUNTIME)
@Target(METHOD)
@AdviceAnnotation
public @interface Sudo {

}
