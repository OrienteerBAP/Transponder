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
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import net.bytebuddy.implementation.bind.MethodDelegationBinder.ParameterBinding;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.constant.JavaConstantValue;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaConstant;

/**
 * ByteBuddy binder to resolve/translate with the aid of {@link IPolyglot} actual query/command to be executed.
 * Can be used only over <code>String[]</code> and designed to work only with {@link Query}, {@link Lookup}, {@link Command}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface QueryValue {

	/**
	 * Actual binder for {@link QueryValue}. Should be instantiated everytime, because it requires instance of {@link Transponder}
	 */
	public static class Binder implements TargetMethodAnnotationDrivenBinder.ParameterBinder<QueryValue> {
		
		private final Transponder transponder;
		
		private static final ElementMatcher<AnnotationDescription> MATCHER = 
				ElementMatchers.annotationType(Query.class)
			.or(ElementMatchers.annotationType(Lookup.class))
			.or(ElementMatchers.annotationType(Command.class));
		
		/**
		 * Creates the binder for {@link QueryValue}. Transponder is required to be able to translate queries
		 * @param transponder transponder instance to be associated with current binder
		 */
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
			Class<?> ownerClass = safeClassForName(source.getDeclaringType().getTypeName());
			final String queryId = defaultIfNullOrEmpty(ann.getValue("id").resolve(String.class), 
								() -> source.getDeclaringType().getTypeName()+"."+source.getName());
			final String language = emptyToNull(ann.getValue("language").resolve(String.class));
			final String value = emptyToNull(ann.getValue("value").resolve(String.class));
			final String dialect = emptyToNull(ann.getValue("dialect").resolve(String.class));
			
			IPolyglot.Translation translation = transponder.getPolyglot()
								.translate(ownerClass, queryId, language, value, dialect, transponder.getDriver().getDialect());
			if(translation==null)
				return new ParameterBinding<Void>() {
					@Override
					public boolean isValid() {
						return false;
					}
					@Override
					public Size apply(MethodVisitor methodVisitor, Context implementationContext) {
						throw new IllegalStateException(String.format("Translation was not found for key=%s and dialect=%s", queryId, dialect));
					}
					@Override
					public Void getIdentificationToken() {
						throw new IllegalStateException(String.format("Translation was not found for key=%s and dialect=%s", queryId, dialect));
					}
			};
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
