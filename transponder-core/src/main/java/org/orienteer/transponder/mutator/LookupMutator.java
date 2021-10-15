package org.orienteer.transponder.mutator;

import static org.orienteer.transponder.CommonUtils.*;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.Transponder.ITransponderEntity;
import org.orienteer.transponder.annotation.Lookup;
import org.orienteer.transponder.annotation.binder.QueryValue;

import com.google.common.primitives.Primitives;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;
import java.util.Map;

public class LookupMutator implements IMutator {

	@Override
	public void schedule(Transponder transponder, BuilderScheduler scheduler) {
		scheduler.scheduleDelegate(isAnnotatedWith(Lookup.class).and(isAbstract()),
								   LookupDelegate.class,
								   QueryValue.Binder.INSTANCE);
	}
	
	public static class LookupDelegate {
		@RuntimeType
		public static Object query(@QueryValue String[] query, @Origin Method origin, @This Object thisObject, @AllArguments Object[] args) {
			Map<String, Object> params = toArguments(origin, args);
			if(thisObject instanceof ITransponderEntity) params.put("target", Transponder.unwrap(thisObject));
			Transponder transponder = Transponder.getTransponder(thisObject);
			Object newSeed = transponder.getDriver().querySingle(query[1], query[0], params);
			if(newSeed!=null) {
				if(transponder.getDriver().isSeed(newSeed)) {
					if(thisObject instanceof ITransponderEntity)
						transponder.getDriver().replaceSeed(thisObject, newSeed);
				} else
					throw new IllegalStateException("Result of a lookup can't be used as new seed object. Looked up object: "+newSeed);
			}
			if(Primitives.wrap(origin.getReturnType()).equals(Boolean.class)) 
				return newSeed!=null;
			else if(newSeed==null) return null;
			else if(thisObject instanceof ITransponderEntity) return thisObject;
			else return transponder.wrap(newSeed, origin.getGenericReturnType());
		}
	}

}
