package org.orienteer.transponder.annotation.binder;

import static org.orienteer.transponder.CommonUtils.*;
import static com.google.common.base.Strings.emptyToNull;
import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.orienteer.transponder.CommonUtils;
import org.orienteer.transponder.IPolyglot;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.annotation.Command;
import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.Lookup;
import org.orienteer.transponder.annotation.Query;

import com.google.common.base.Strings;

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
import net.bytebuddy.implementation.bytecode.StackManipulation;
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

	public static class Binder implements TargetMethodAnnotationDrivenBinder.ParameterBinder<QueryValue> {
		
		private final Transponder transponder;
		
		public static final ElementMatcher<AnnotationDescription> MATCHER = 
				ElementMatchers.annotationType(Query.class)
			.or(ElementMatchers.annotationType(Lookup.class))
			.or(ElementMatchers.annotationType(Command.class));
		
		public Binder(Transponder transponder) {
			this.transponder = transponder;
		}

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
			String value = emptyToNull(ann.getValue("value").resolve(String.class));
			String language = emptyToNull(ann.getValue("language").resolve(String.class));
			String dialect = emptyToNull(ann.getValue("dialect").resolve(String.class));
			String queryId = source.getDeclaringType().getTypeName()+"."+source.getName();
			
			IPolyglot.Translation translation = transponder.getPolyglot()
								.translate(queryId, language, dialect, value, transponder.getDriver());
			
			return new MethodDelegationBinder.ParameterBinding.Anonymous(
				createStringArray(translation.getQuery(), translation.getLanguage())
				);
		}
		
		private static StackManipulation createStringArray(String... array) {
			List<JavaConstantValue> values = new ArrayList<>();
			for (String value : array) 
				values.add(new JavaConstantValue(JavaConstant.Simple.ofLoaded(CommonUtils.defaultIfNull(value, ""))));
			
			return ArrayFactory.forType(TypeDescription.ForLoadedType.of(String.class).asGenericType()).withValues(values);
		}
		
	}
}
