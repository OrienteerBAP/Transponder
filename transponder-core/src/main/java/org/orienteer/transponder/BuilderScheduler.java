package org.orienteer.transponder;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.orienteer.transponder.CommonUtils.*;

import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.orienteer.transponder.annotation.AdviceAnnotation;
import org.orienteer.transponder.annotation.DelegateAnnotation;
import org.orienteer.transponder.annotation.OverrideByThis;

import lombok.Value;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.SignatureToken;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.implementation.DefaultMethodCall;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Class which helps independently define in declarative way multiple {@link Advice}s and {@link ImplementationDefinition}
 * and then combine them properly for ByteBuddy. For example: all associated {@link Advice}s will be wrapped over each other.
 */
public class BuilderScheduler {
	
	@Value
	private static class Case {
		private ElementMatcher<? super MethodDescription> matcher;
		private Implementation implementation;
	}
	
	private List<Case> cases = new ArrayList<>();
	
	/**
	 * Schedule implementation for matching methods
	 * @param matcher matcher to select methods to cover be provided implementation
	 * @param implementation implementation to be used for selected methods
	 * @return this {@link BuilderScheduler} for chaining
	 */
	public BuilderScheduler schedule(ElementMatcher<? super MethodDescription> matcher, Implementation implementation) {
		cases.add(new Case(matcher, implementation));
		return this;
	}
	
	/**
	 * Schedule implementation/advice for all methods annotated by provided annotation.
	 * Annotation should have details about actual delegate
	 * @param annotationClass annotation class to be used for method selection.
	 * Also it should contains definition of an delegate through {@link DelegateAnnotation} or {@link AdviceAnnotation}
	 * @return this {@link BuilderScheduler} for chaining
	 */
	public BuilderScheduler schedule(Class<? extends Annotation> annotationClass) {
		return schedule(annotationClass, null);
	}
	
	/**
	 * Schedule implementation/advice for all methods annotated by provided annotation.
	 * Annotation should have details about actual delegate
	 * @param annotationClass annotation class to be used for method selection.
	 * Also it should contains definition of an delegate through {@link DelegateAnnotation} or {@link AdviceAnnotation}
	 * @param delegate class to delegate to - might be null
	 * @return this {@link BuilderScheduler} for chaining
	 */
	public BuilderScheduler schedule(Class<? extends Annotation> annotationClass, Class<?> delegate) {
		DelegateAnnotation delegateAnnotation = annotationClass.getAnnotation(DelegateAnnotation.class);
		if(delegateAnnotation==null && delegate!=null)
			delegateAnnotation = delegate.getAnnotation(DelegateAnnotation.class);
		if(delegateAnnotation!=null) {
			if(delegate==null) delegate = delegateAnnotation.value();
			return scheduleDelegate(annotationClass, delegate);
		}
		AdviceAnnotation adviceAnnotation = annotationClass.getAnnotation(AdviceAnnotation.class);
		if(adviceAnnotation==null && delegate!=null)
			adviceAnnotation = delegate.getAnnotation(AdviceAnnotation.class);
		if(adviceAnnotation!=null) {
			if(delegate==null) delegate = adviceAnnotation.value();
			return scheduleAdvice(annotationClass, delegate);
		}
		throw new IllegalStateException("Annotation '"+annotationClass.getName()
											+"' is not providing information about delegate to advice");
	}
	
	/**
	 * Schedule {@link MethodDelegation} implementation for selected methods defined by matcher to provided ByteBuddy delegate
	 * @param matcher matcher to select methods to cover be provided implementation
	 * @param delegate class to be used for delegation
	 * @return this {@link BuilderScheduler} for chaining
	 */
	public BuilderScheduler scheduleDelegate(ElementMatcher<? super MethodDescription> matcher, Class<?> delegate) {
		cases.add(new Case(matcher, MethodDelegation.to(delegate)));
		return this;
	}
	
	/**
	 * Schedule {@link MethodDelegation} implementation for selected methods defined by matcher to provided ByteBuddy delegate
	 * with provided set of binders
	 * @param matcher matcher to select methods to cover be provided implementation
	 * @param delegate class to be used for delegation
	 * @param parameterBinders set of binders to be used for scheduling
	 * @return this {@link BuilderScheduler} for chaining
	 */
	public BuilderScheduler scheduleDelegate(ElementMatcher<? super MethodDescription> matcher, Class<?> delegate, TargetMethodAnnotationDrivenBinder.ParameterBinder<?>... parameterBinders) {
		cases.add(new Case(matcher, MethodDelegation.withDefaultConfiguration().withBinders(parameterBinders).to(delegate)));
		return this;
	}
	
	/**
	 * Schedule {@link MethodDelegation} implementation for selected methods annotated by provided annotation class to provided ByteBuddy delegate
	 * @param annotationClass annotation to be used to select methods to cover be provided implementation
	 * @param delegate class to be used for delegation
	 * @return this {@link BuilderScheduler} for chaining
	 */
	public BuilderScheduler scheduleDelegate(Class<? extends Annotation> annotationClass, Class<?> delegate) {
		return scheduleDelegate(isAnnotatedWith(annotationClass), delegate);
	}
	
	/**
	 * Schedule {@link Advice} for selected methods defined by matcher to provided ByteBuddy delegate
	 * @param matcher matcher to select methods to cover be provided implementation
	 * @param advice class to be used for {@link Advice} delegation
	 * @return this {@link BuilderScheduler} for chaining
	 */
	public BuilderScheduler scheduleAdvice(ElementMatcher<? super MethodDescription> matcher, Class<?> advice) {
		cases.add(new Case(matcher, Advice.to(advice)));
		return this;
	}
	
	/**
	 * Schedule {@link Advice} for selected methods annotated by provided annotation class to provided ByteBuddy delegate
	 * @param annotationClass annotation to be used to select methods to cover be provided implementation
	 * @param advice class to be used for {@link Advice} delegation
	 * @return this {@link BuilderScheduler} for chaining
	 */
	public BuilderScheduler scheduleAdvice(Class<? extends Annotation> annotationClass, Class<?> advice) {
		return scheduleAdvice(isAnnotatedWith(annotationClass), advice);
	}
	
	/**
	 * Executed at the end of {@link IMutator}s chain to apply all scheduled changes in the builder
	 * @param <T> type of the class to be built
	 * @param builder ByteBuddy builder
	 * @return new instance of {@link DynamicType.Builder} which contains all required changes
	 */
	public <T> DynamicType.Builder<T> apply(DynamicType.Builder<T> builder) {
		TypeDescription description = builder.toTypeDescription();
		List<MethodDescription> methods = getMethodDescriptionList(description);
		enhanceCasesByDynamicDefinitions(methods);
		builder = enhanceCasesByOverrides(builder, description, methods);
		if(cases.isEmpty()) return builder;
		Case[] cases = this.cases.toArray(new Case[this.cases.size()]);
		//Lets cache presence of methods per condition
		boolean[] hasMatchedMethods = new boolean[cases.length];
		boolean[] hasOpositeMatchedMethods = new boolean[cases.length];
		for (int i=0; i<cases.length; i++) {
			hasMatchedMethods[i] = hasMatch(methods, cases[i].getMatcher());
			hasOpositeMatchedMethods[i] = hasMatch(methods, not(cases[i].getMatcher()));
		}
		
		BigInteger permutationsTotal = BigInteger.ONE.shiftLeft(cases.length).subtract(BigInteger.ONE);
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
				boolean caseIncluded = currentPermutation.testBit(i); // Included or excluded
				if(!caseIncluded) continue; //Advice will not be included in any case: moving forward
				if(!(caseIncluded?hasMatchedMethods[i]:hasOpositeMatchedMethods[i])) break; //No such methods at all
				matcher = matcher.and(caseIncluded?c.getMatcher():not(c.getMatcher()));
				if(caseIncluded 
						&& c.getImplementation() instanceof Advice
						&& c.getImplementation() != baseImplementation) {
					implementation = ((Advice)c.getImplementation()).wrap(implementation);
				}
			}
			if(i<0)
				builder = builder.method(matcher).intercept(implementation);
		}
		return builder;
	}
	
	protected void enhanceCasesByDynamicDefinitions(List<MethodDescription> methods) {
		ClassLoader cl = BuilderScheduler.class.getClassLoader();
		methods.stream().filter(m -> isAnnotatedWith(DelegateAnnotation.class).matches(m))
			    .flatMap(m -> m.getDeclaredAnnotations().filter(annotationType(DelegateAnnotation.class)).stream())
			    .distinct().forEachOrdered(a -> {
			    	schedule(declaresAnnotation(is(a)), MethodDelegation.to(a.getValue("value").load(cl).resolve(Class.class)));
			    });
		methods.stream().filter(m -> isAnnotatedWith(AdviceAnnotation.class).or(isAnnotatedWith(AdviceAnnotation.List.class)).matches(m))
			    .flatMap(m -> m.getDeclaredAnnotations().filter(annotationType(AdviceAnnotation.class).or(annotationType(AdviceAnnotation.List.class))).stream())
			    .flatMap(a -> {
			    	Object subA = a.getValue("value").resolve();
			    	if(subA.getClass().isArray())
			    		return Stream.of((AnnotationDescription[])subA);
			    	else
			    		return Stream.of(a);
			    })
			    .distinct().forEachOrdered(a -> {
			    	schedule(hasRepeatableAnnotation(a, new TypeDescription.ForLoadedType(AdviceAnnotation.List.class)),
			    			Advice.to(a.getValue("value").load(cl).resolve(Class.class)));
			    });
			
	}
	
	private <T> DynamicType.Builder<T> enhanceCasesByOverrides(DynamicType.Builder<T> builder, TypeDescription type,  List<MethodDescription> methods) {
		Map<SignatureToken, MethodDescription> signatures = methods.stream()
												.collect(Collectors.toMap(m->m.asSignatureToken(), m->m, (k1, k2) -> k1));
		List<Case> casesToAdd = type.getInterfaces().stream()
			.flatMap(i->CommonUtils.getMethodDescriptionList(i).stream())
			.filter(OverrideByThis.ANNOTATED_BY_THIS_MATCHER::matches)
			.filter(m -> {
				//Checking that original method is present per signature
				//and that it's not exactly the same method as we are trying to use to override
				MethodDescription origM = signatures.get(m.asSignatureToken());
				return origM!=null && !origM.equals(m);
			})
			.peek(m-> {
				if(m.isDefaultMethod())
					schedule(ElementMatchers.hasSignature(m.asSignatureToken()), 
							DefaultMethodCall.prioritize(m.getDeclaringType().asErasure()));
			}).flatMap(m -> cases.stream().filter(c-> c.getMatcher().matches(m))
							.map(c -> new Case(ElementMatchers.hasSignature(m.asSignatureToken()), c.getImplementation()))
			).collect(Collectors.toList());
		cases.addAll(casesToAdd);
		return builder;
	}

	
}
