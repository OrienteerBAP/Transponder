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

public class GetterMutator implements IMutator {

	@Override
	public void schedule(Transponder transponder, BuilderScheduler scheduler) {
		scheduler.scheduleDelegate(isGetter().and(isAbstract()), GetDelegate.class, PropertyName.Binder.INSTANCE);
	}
	
	public static class GetDelegate {
		@RuntimeType
		public static Object getValue(@PropertyName String property, @This Object wrapper, @Origin Method method) {
			Transponder transponder = Transponder.getTransponder(wrapper);
			return transponder.wrap(transponder.getDriver().getPropertyValue(wrapper, property), method.getGenericReturnType());
		}
	}

}
