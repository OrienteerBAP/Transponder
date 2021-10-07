package org.orienteer.transponder.mutator;

import static org.orienteer.transponder.CommonUtils.*;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.Transponder.ITransponderHolder;
import org.orienteer.transponder.annotation.Query;

import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

public class QueryMutator implements IMutator {

	@Override
	public <T> Builder<T> mutate(Transponder transponder, Builder<T> builder) {
		return builder.method(isAnnotatedWith(Query.class).and(isAbstract()))
							.intercept(MethodDelegation
										.withDefaultConfiguration()
										.to(QueryDelegate.class));
	}
	
	public static class QueryDelegate {
		@RuntimeType
		public static Object query(@Origin Method origin, @This Object thisObject, @AllArguments Object[] args) {
			try {
				Query query = origin.getAnnotation(Query.class);
				Map<String, Object> params = toArguments(origin, args);
				if(thisObject instanceof ITransponderHolder) params.put("target", Transponder.unwrap(thisObject));
				Transponder transponder = Transponder.getTransponder(thisObject);
				Object ret = Collection.class.isAssignableFrom(origin.getReturnType())
									?transponder.getDriver().query(query, params)
									:transponder.getDriver().querySingle(query, params);
				return transponder.wrap(ret, origin.getGenericReturnType());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

}
