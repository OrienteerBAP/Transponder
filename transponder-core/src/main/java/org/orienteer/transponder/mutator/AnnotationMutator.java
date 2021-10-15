package org.orienteer.transponder.mutator;

import java.lang.annotation.Annotation;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;

public class AnnotationMutator implements IMutator {
	private final Class<? extends Annotation> annotationClass;
	
	public AnnotationMutator(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}
	
	@Override
	public void schedule(Transponder transponder, BuilderScheduler scheduler) {
		scheduler.schedule(annotationClass);
	}
}
