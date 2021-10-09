package org.orienteer.transponder.orientdb;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.orientechnologies.orient.core.collate.ODefaultCollate;
import com.orientechnologies.orient.core.metadata.schema.OType;

@Retention(RUNTIME)
@Target(METHOD)
public @interface OrientDBProperty {
	OType type() default OType.ANY;
	OType linkedType() default OType.ANY;
	String linkedClass() default "";
	boolean embedded() default false;
	boolean notNull() default false;
	boolean mandatory() default false;
	boolean readOnly() default false;
	String min() default "";
	String max() default "";
	String regexp() default "";
	String collate() default ODefaultCollate.NAME;
	String defaultValue() default "";
}
