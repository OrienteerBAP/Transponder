package org.orienteer.transponder.annotation.binder;

import static org.orienteer.transponder.CommonUtils.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.orienteer.transponder.annotation.Command;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.Lookup;
import org.orienteer.transponder.annotation.Query;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.annotation.AnnotationDescription.Loadable;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import net.bytebuddy.implementation.bind.MethodDelegationBinder.ParameterBinding;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.implementation.bytecode.constant.JavaConstantValue;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaConstant;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryLanguage {

	enum Binder implements TargetMethodAnnotationDrivenBinder.ParameterBinder<QueryLanguage> {
		INSTANCE;
		
		@Override
		public Class<QueryLanguage> getHandledType() {
			return QueryLanguage.class;
		}

		@Override
		public ParameterBinding<?> bind(Loadable<QueryLanguage> annotation, MethodDescription source,
				ParameterDescription target, net.bytebuddy.implementation.Implementation.Target implementationTarget,
				Assigner assigner, Typing typing) {
			AnnotationList annotations = source.getDeclaredAnnotations().filter(QueryValue.Binder.MATCHER);
			if(annotations==null || annotations.size()!=1)
				return MethodDelegationBinder.ParameterBinding.Illegal.INSTANCE;
			AnnotationDescription ann = annotations.get(0);
			String value = ann.getValue("language").resolve(String.class);
			return new MethodDelegationBinder.ParameterBinding.Anonymous(new JavaConstantValue(JavaConstant.Simple.ofLoaded(value)));
		}
		
	}
}
