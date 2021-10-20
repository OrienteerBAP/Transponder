package org.orienteer.transponder;

import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.orienteer.transponder.annotation.AdviceAnnotation;
import org.orienteer.transponder.annotation.DelegateAnnotation;

import lombok.Value;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder;
import net.bytebuddy.matcher.ElementMatcher;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.orienteer.transponder.CommonUtils.*;

public class BuilderScheduler {
	
	@Value
	private static class Case {
		private ElementMatcher<? super MethodDescription> matcher;
		private Implementation implementation;
	}
	
	private List<Case> cases = new ArrayList<>();
	
	
	public BuilderScheduler schedule(ElementMatcher<? super MethodDescription> matcher, Implementation implementation) {
		cases.add(new Case(matcher, implementation));
		return this;
	}
	
	public BuilderScheduler schedule(Class<? extends Annotation> annotationClass) {
		DelegateAnnotation delegateAnnotation = annotationClass.getAnnotation(DelegateAnnotation.class);
		if(delegateAnnotation!=null) {
			return scheduleDelegate(annotationClass, delegateAnnotation.value());
		}
		AdviceAnnotation adviceAnnotation = annotationClass.getAnnotation(AdviceAnnotation.class);
		if(adviceAnnotation!=null) {
			return scheduleAdvice(annotationClass, adviceAnnotation.value());
		}
		throw new IllegalStateException("Annotation '"+annotationClass.getName()
											+"' is not providing information about delegate to advice");
	}
	
	public BuilderScheduler scheduleDelegate(ElementMatcher<? super MethodDescription> matcher, Class<?> delegate) {
		cases.add(new Case(matcher, MethodDelegation.to(delegate)));
		return this;
	}
	
	public BuilderScheduler scheduleDelegate(ElementMatcher<? super MethodDescription> matcher, Class<?> delegate, TargetMethodAnnotationDrivenBinder.ParameterBinder<?>... parameterBinders) {
		cases.add(new Case(matcher, MethodDelegation.withDefaultConfiguration().withBinders(parameterBinders).to(delegate)));
		return this;
	}
	
	public BuilderScheduler scheduleDelegate(Class<? extends Annotation> annotationClass, Class<?> delegate) {
		return scheduleDelegate(isAnnotatedWith(annotationClass), delegate);
	}
	
	public BuilderScheduler scheduleAdvice(ElementMatcher<? super MethodDescription> matcher, Class<?> advice) {
		cases.add(new Case(matcher, Advice.to(advice)));
		return this;
	}
	
	public BuilderScheduler scheduleAdvice(Class<? extends Annotation> annotationClass, Class<?> advice) {
		return scheduleAdvice(isAnnotatedWith(annotationClass), advice);
	}
	
	public <T> DynamicType.Builder<T> apply(DynamicType.Builder<T> builder) {
		if(cases.isEmpty()) return builder;
		Case[] cases = this.cases.toArray(new Case[this.cases.size()]);
		TypeDescription description = builder.toTypeDescription();
		List<MethodDescription> methods = getMethodDescriptionList(description);
		BigInteger permutationsTotal = BigInteger.ONE.shiftLeft(cases.length-1);
		for(BigInteger currentPermutation = BigInteger.ONE; 
				currentPermutation.compareTo(permutationsTotal)<=0;
				currentPermutation = currentPermutation.add(BigInteger.ONE)) {
			//Finding Base Implementation: mostly recently added implementation (not Advice)
			Implementation baseImplementation = null;
			for(int i=cases.length-1; i>=0; i--) {
				if(currentPermutation.testBit(i)) {
					Implementation impl = cases[i].getImplementation();
					if(baseImplementation==null) baseImplementation=cases[i].getImplementation();
					if(!(impl instanceof Advice)) {
						baseImplementation = impl;
						break;
					}
				}
			}
			ElementMatcher.Junction<? super MethodDescription> matcher = any();
			Implementation implementation = baseImplementation;
			int i = cases.length-1;
			for(; i>=0; i--) {
				Case c = cases[i];
				ElementMatcher<? super MethodDescription> thisMatcher = c.getMatcher();
				if(!currentPermutation.testBit(i)) thisMatcher = not(thisMatcher);
				if(!hasMatch(methods, thisMatcher)) break; // Adding conditions will not make any difference
				matcher = matcher.and(thisMatcher);
				if(currentPermutation.testBit(i) && c.getImplementation() instanceof Advice) {
					implementation = ((Advice)c.getImplementation()).wrap(implementation);
				}
			}
			if(i<0)
				builder = builder.method(matcher).intercept(implementation);
		}
		return builder;
	}

	
}
