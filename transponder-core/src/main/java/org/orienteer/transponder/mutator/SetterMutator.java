package org.orienteer.transponder.mutator;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;

import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.annotation.binder.PropertyName;

import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

public class SetterMutator implements IMutator {

	@Override
	public <T> Builder<T> mutate(Transponder transponder, Builder<T> builder) {
		return builder.method(isSetter().and(isAbstract()))
				.intercept(MethodDelegation.withDefaultConfiguration()
											.withBinders(PropertyName.Binder.INSTANCE)
											.to(SetDelegate.class));
	}
	
	public static class SetDelegate {
		@RuntimeType
		public static Object getValue(@PropertyName String property, @This Object wrapper, @Origin Method method, @Argument(0) Object value) {
			Transponder transponder = Transponder.getTransponder(wrapper);
			transponder.getDriver().setPropertyValue(wrapper, property, transponder.unwrap(value));
			return null;
		}
	}

}
