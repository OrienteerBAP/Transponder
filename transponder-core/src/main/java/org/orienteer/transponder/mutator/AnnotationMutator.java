package org.orienteer.transponder.mutator;

import java.lang.annotation.Annotation;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;

public class AnnotationMutator implements IMutator {
	private final Class<? extends Annotation> annotationClass;
	
	public AnnotationMutator(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}
	
	@Override
	public void schedule(BuilderScheduler scheduler) {
		scheduler.schedule(annotationClass);
	}
}