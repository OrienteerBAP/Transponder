package org.orienteer.transponder.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.bytebuddy.description.annotation.AnnotationSource;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Annotation for methods to enforce overriding by this method
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface OverrideByThis {
	
	public static final ElementMatcher<AnnotationSource> ANNOTATED_BY_THIS_MATCHER 
								= ElementMatchers.isAnnotatedWith(OverrideByThis.class);
}
