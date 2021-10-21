package org.orienteer.transponder.mutator;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.annotation.binder.PropertyName;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * {@link IMutator} to implement setters methods
 */
public class SetterMutator implements IMutator {

	@Override
	public void schedule(Transponder transponder, BuilderScheduler scheduler) {
		scheduler.scheduleDelegate(nameStartsWith("set")
					.and(takesArguments(1))
					.and(isAbstract()), SetDelegate.class, PropertyName.Binder.INSTANCE);
	}
	
	/**
	 * ByteBuddy delegate
	 */
	public static class SetDelegate {
		/**
		 * Sets value of a propertys
		 * @param property name of property to set value to
		 * @param wrapper wrapper object
		 * @param method original method to support chaining
		 * @param value actual value to set
		 * @return null or wrapper object: depends on method definition
		 */
		@RuntimeType
		public static Object setValue(@PropertyName String property, @This Object wrapper, @Origin Method method, @Argument(0) Object value) {
			Transponder transponder = Transponder.getTransponder(wrapper);
			transponder.getDriver().setPropertyValue(wrapper, property, Transponder.unwrap(value));
			if(method.getReturnType().isInstance(wrapper)) return wrapper;
			else return null;
		}
	}

}
