package org.orienteer.transponder.mutator;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.annotation.binder.PropertyName;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.StubValue;
import net.bytebuddy.implementation.bind.annotation.This;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * {@link IMutator} to implement getters methods
 */
public class GetterMutator implements IMutator {

	@Override
	public void schedule(Transponder transponder, BuilderScheduler scheduler) {
		scheduler.scheduleDelegate(isGetter().and(isAbstract()), GetDelegate.class, PropertyName.Binder.INSTANCE);
	}
	
	/**
	 * ByteBuddy delegate
	 */
	public static class GetDelegate {
		/**
		 * Obtain value of the property
		 * @param property name of property to get value of
		 * @param wrapper wrapper object
		 * @param method original method to support dynamic casting
		 * @param stubValue value which needs to be returned if property value is null
		 * @return property value
		 */
		@RuntimeType
		public static Object getValue(@PropertyName String property, @This Object wrapper, @Origin Method method, @StubValue Object stubValue) {
			Transponder transponder = Transponder.getTransponder(wrapper);
			Type type = method.getGenericReturnType();
			Object ret = transponder.wrap(transponder.getDriver().getPropertyValue(wrapper, property, type), type);
			return ret !=null ? ret : stubValue;
		}
	}

}
