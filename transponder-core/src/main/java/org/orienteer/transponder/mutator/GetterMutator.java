package org.orienteer.transponder.mutator;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.annotation.binder.PropertyName;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;

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
		 * @return property value
		 */
		@RuntimeType
		public static Object getValue(@PropertyName String property, @This Object wrapper, @Origin Method method) {
			Transponder transponder = Transponder.getTransponder(wrapper);
			return transponder.wrap(transponder.getDriver().getPropertyValue(wrapper, property), method.getGenericReturnType());
		}
	}

}
