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

public class SetterMutator implements IMutator {

	@Override
	public void schedule(Transponder transponder, BuilderScheduler scheduler) {
		scheduler.scheduleDelegate(nameStartsWith("set")
					.and(takesArguments(1))
					.and(isAbstract()), SetDelegate.class, PropertyName.Binder.INSTANCE);
	}
	
	public static class SetDelegate {
		@RuntimeType
		public static Object getValue(@PropertyName String property, @This Object wrapper, @Origin Method method, @Argument(0) Object value) {
			Transponder transponder = Transponder.getTransponder(wrapper);
			transponder.getDriver().setPropertyValue(wrapper, property, Transponder.unwrap(value));
			if(method.getReturnType().isInstance(wrapper)) return wrapper;
			else return null;
		}
	}

}
