package org.orienteer.transponder.mutator;

import static org.orienteer.transponder.CommonUtils.*;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.Transponder.ITransponderEntity;
import org.orienteer.transponder.annotation.Command;

import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.lang.reflect.Method;
import java.util.Map;

public class CommandMutator implements IMutator {

	@Override
	public <T> Builder<T> mutate(Transponder transponder, Builder<T> builder) {
		return builder.method(isAnnotatedWith(Command.class).and(isAbstract()))
							.intercept(MethodDelegation
										.withDefaultConfiguration()
										.to(CommandDelegate.class));
	}
	
	public static class CommandDelegate {
		@RuntimeType
		public static Object query(@Origin Method origin, @This Object thisObject, @AllArguments Object[] args) {
			try {
				Command command = origin.getAnnotation(Command.class);
				Map<String, Object> params = toArguments(origin, args);
				if(thisObject instanceof ITransponderEntity) params.put("target", Transponder.unwrap(thisObject));
				Transponder transponder = Transponder.getTransponder(thisObject);
				Object ret = transponder.getDriver().command(command.language(), command.value(), params);
				return transponder.wrap(ret, origin.getGenericReturnType());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

}
