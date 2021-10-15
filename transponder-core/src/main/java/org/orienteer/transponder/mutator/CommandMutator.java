package org.orienteer.transponder.mutator;

import static org.orienteer.transponder.CommonUtils.*;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.Transponder.ITransponderEntity;
import org.orienteer.transponder.annotation.Command;
import org.orienteer.transponder.annotation.binder.QueryValue;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;
import java.util.Map;

public class CommandMutator implements IMutator {

	@Override
	public void schedule(Transponder transponder, BuilderScheduler scheduler) {
		scheduler.scheduleDelegate(isAnnotatedWith(Command.class).and(isAbstract()),
								   CommandDelegate.class,
								   QueryValue.Binder.INSTANCE);
	}
	
	public static class CommandDelegate {
		@RuntimeType
		public static Object query(@QueryValue String[] query, @Origin Method origin, @This Object thisObject, @AllArguments Object[] args) {
			try {
				Map<String, Object> params = toArguments(origin, args);
				if(thisObject instanceof ITransponderEntity) params.put("target", Transponder.unwrap(thisObject));
				Transponder transponder = Transponder.getTransponder(thisObject);
				Object ret = transponder.getDriver().command(query[1], query[0], params);
				return transponder.wrap(ret, origin.getGenericReturnType());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

}
