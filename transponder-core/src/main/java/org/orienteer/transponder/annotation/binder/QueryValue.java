package org.orienteer.transponder.annotation.binder;

import static org.orienteer.transponder.CommonUtils.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import org.orienteer.transponder.annotation.Command;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.Lookup;
import org.orienteer.transponder.annotation.Query;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.annotation.AnnotationValue.ForTypeDescription;
import net.bytebuddy.description.annotation.AnnotationDescription.Loadable;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import net.bytebuddy.implementation.bind.MethodDelegationBinder.ParameterBinding;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.constant.JavaConstantValue;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaConstant;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryValue {

	enum Binder implements TargetMethodAnnotationDrivenBinder.ParameterBinder<QueryValue> {
		INSTANCE;
		
		public static final ElementMatcher<AnnotationDescription> MATCHER = 
				ElementMatchers.annotationType(Query.class)
			.or(ElementMatchers.annotationType(Lookup.class))
			.or(ElementMatchers.annotationType(Command.class));

		@Override
		public Class<QueryValue> getHandledType() {
			return QueryValue.class;
		}

		@Override
		public ParameterBinding<?> bind(Loadable<QueryValue> annotation, MethodDescription source,
				ParameterDescription target, net.bytebuddy.implementation.Implementation.Target implementationTarget,
				Assigner assigner, Typing typing) {
			AnnotationList annotations = source.getDeclaredAnnotations().filter(MATCHER);
			if(annotations==null || annotations.size()!=1)
				return MethodDelegationBinder.ParameterBinding.Illegal.INSTANCE;
			AnnotationDescription ann = annotations.get(0);
			String value = ann.getValue("value").resolve(String.class);
			String language = ann.getValue("language").resolve(String.class);
			String dialect = ann.getValue("dialect").resolve(String.class);
			
			return new MethodDelegationBinder.ParameterBinding.Anonymous(
				ArrayFactory.forType(TypeDescription.ForLoadedType.of(String.class).asGenericType())
					.withValues(Arrays.asList(new JavaConstantValue(JavaConstant.Simple.ofLoaded(value)),
							new JavaConstantValue(JavaConstant.Simple.ofLoaded(language)),
							new JavaConstantValue(JavaConstant.Simple.ofLoaded(dialect))))
				);
		}
		
	}
}
