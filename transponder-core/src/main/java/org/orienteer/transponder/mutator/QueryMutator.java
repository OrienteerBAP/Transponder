package org.orienteer.transponder.mutator;

import static org.orienteer.transponder.CommonUtils.*;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.Transponder.ITransponderEntity;
import org.orienteer.transponder.annotation.Command;
import org.orienteer.transponder.annotation.Query;
import org.orienteer.transponder.annotation.binder.QueryValue;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * {@link IMutator} and delegate to implement methods annotated by {@link Query}
 */
public class QueryMutator implements IMutator {

	@Override
	public void schedule(Transponder transponder, BuilderScheduler scheduler) {
		scheduler.scheduleDelegate(isAnnotatedWith(Query.class).and(isAbstract()),
								   QueryDelegate.class,
								   new QueryValue.Binder(transponder));
	}
	
	/**
	 * ByteBuddy delegate for {@link Query} processing
	 */
	public static class QueryDelegate {
		/**
		 * ByteBuddy delegate
		 * @param query already translated query
		 * @param origin original method to support dynamic casting
		 * @param thisObject wrapper object
		 * @param args array with all arguments
		 * @return result of the query
		 */
		@RuntimeType
		public static Object executeQuery(@QueryValue String[] query, @Origin Method origin, @This Object thisObject, @AllArguments Object[] args) {
			Map<String, Object> params = toArguments(origin, args);
			if(thisObject instanceof ITransponderEntity) {
				params.put("target", Transponder.unwrap(thisObject));
				params.put("targetType", resolveEntityType(thisObject.getClass()));
			}
			Transponder transponder = Transponder.getTransponder(thisObject);
			Type type = origin.getGenericReturnType();
			Object ret = Collection.class.isAssignableFrom(origin.getReturnType())
								?transponder.getDriver().query(query[1], query[0], params, type)
								:transponder.getDriver().querySingle(query[1], query[0], params, type);
			return transponder.wrap(ret, origin.getGenericReturnType());
		}
	}

}
