package org.orienteer.transponder.mutator;

import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.annotation.binder.PropertyName;

import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;
import java.util.Map;

public class GetterMutator implements IMutator {

	@Override
	public <T> Builder<T> mutate(Transponder transponder, Builder<T> builder) {
		return builder.method(isGetter().and(isAbstract()))
				.intercept(MethodDelegation.withDefaultConfiguration()
								.withBinders(PropertyName.Binder.INSTANCE)
								.to(GetDelegate.class));
	}
	
	public static class GetDelegate {
		@RuntimeType
		public static Object getValue(@PropertyName String property, @This Object wrapper, @Origin Method method) {
			Transponder transponder = Transponder.getTransponder(wrapper);
			return transponder.wrap(transponder.getDriver().getPropertyValue(wrapper, property), method.getGenericReturnType());
		}
	}

}
