package org.orienteer.transponder.annotation.binder;

import static org.orienteer.transponder.CommonUtils.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.orienteer.transponder.annotation.EntityProperty;

import com.google.common.base.Strings;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.annotation.AnnotationDescription.Loadable;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import net.bytebuddy.implementation.bind.MethodDelegationBinder.ParameterBinding;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.Assigner.Typing;
import net.bytebuddy.implementation.bytecode.constant.JavaConstantValue;
import net.bytebuddy.utility.JavaConstant;

/**
 * ByteBuddy binder to provide property name for getters and setters.
 * Uses name of the method or {@link EntityProperty} annotation to obtain name of a property in target data source.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PropertyName {

	/**
	 * Instance of binder for {@link PropertyName}
	 */
	enum Binder implements TargetMethodAnnotationDrivenBinder.ParameterBinder<PropertyName> {
		INSTANCE;

		@Override
		public Class<PropertyName> getHandledType() {
			return PropertyName.class;
		}

		@Override
		public ParameterBinding<?> bind(Loadable<PropertyName> annotation, MethodDescription source,
				ParameterDescription target, net.bytebuddy.implementation.Implementation.Target implementationTarget,
				Assigner assigner, Typing typing) {
			String methodName = source.getName();
			AnnotationDescription.Loadable<EntityProperty> entityProperty 
						= source.getDeclaredAnnotations().ofType(EntityProperty.class);
			String propertyName = null;
			if(entityProperty!=null) {
				propertyName = entityProperty.load().value();
			}
			if(Strings.isNullOrEmpty(propertyName)) {
				if(methodName.startsWith("set") && source.getParameters().size()==1) {
					propertyName = decapitalize(methodName.substring(3));
				} else if(methodName.startsWith("get") && source.getParameters().size()==0) {
					propertyName = decapitalize(methodName.substring(3));
				} else if(methodName.startsWith("is") && source.getParameters().size()==0) {
					propertyName = decapitalize(methodName.substring(2));
				} else {
					return MethodDelegationBinder.ParameterBinding.Illegal.INSTANCE;
				}
			}
			return new MethodDelegationBinder.ParameterBinding.Anonymous(new JavaConstantValue(JavaConstant.Simple.ofLoaded(propertyName)));
		}
		
	}
}
