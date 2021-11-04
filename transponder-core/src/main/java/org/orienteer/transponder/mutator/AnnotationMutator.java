package org.orienteer.transponder.mutator;

import java.lang.annotation.Annotation;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.annotation.DelegateAnnotation;

/**
 * {@link IMutator} to automatically schedule all annotated methods by specific annotation
 */
public class AnnotationMutator implements IMutator {
	private final Class<? extends Annotation> annotationClass;
	private final Class<?> delegate;
	
	/**
	 * Creates mutator
	 * @param annotationClass class of an annotation to be used for scheduling
	 */
	public AnnotationMutator(Class<? extends Annotation> annotationClass) {
		this(annotationClass, null);
	}
	
	/**
	 * Creates mutator
	 * @param annotationClass class of an annotation to be used for scheduling
	 * @param delegate class to delegate to.
	 * If not null - either annotation or delegate should be annotated
	 * either by AdviceAnnotation or {@link DelegateAnnotation}
	 */
	public AnnotationMutator(Class<? extends Annotation> annotationClass, Class<?> delegate) {
		this.annotationClass = annotationClass;
		this.delegate = delegate;
	}
	
	@Override
	public void schedule(Transponder transponder, BuilderScheduler scheduler) {
		scheduler.schedule(annotationClass, delegate);
	}
}
