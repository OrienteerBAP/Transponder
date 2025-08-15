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

import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * {@link IMutator} and delegate to implement methods annotated by {@link Command}
 */
public class CommandMutator implements IMutator {

	@Override
	public void schedule(Transponder transponder, BuilderScheduler scheduler) {
		// Schedule void methods separately to avoid ByteBuddy delegation issues
		scheduler.scheduleDelegate(isAnnotatedWith(Command.class).and(isAbstract()).and(returns(void.class)),
								   VoidCommandDelegate.class,
								   new QueryValue.Binder(transponder));
		// Schedule non-void methods with the original delegate
		scheduler.scheduleDelegate(isAnnotatedWith(Command.class).and(isAbstract()).and(not(returns(void.class))),
								   CommandDelegate.class,
								   new QueryValue.Binder(transponder));
	}
	
	/**
	 * ByteBuddy delegate for void {@link Command} processing
	 */
	public static class VoidCommandDelegate {
		/**
		 * ByteBuddy delegate for void commands
		 * @param command already translated query for the command
		 * @param origin original method to support dynamic casting
		 * @param thisObject wrapper object
		 * @param args array with all arguments
		 */
		public static void executeVoidCommand(@QueryValue String[] command, @Origin Method origin, @This Object thisObject, @AllArguments Object[] args) {
			Map<String, Object> params = toArguments(origin, args);
			if(thisObject instanceof ITransponderEntity) {
				params.put("target", Transponder.unwrap(thisObject));
				params.put("targetType", resolveEntityType(thisObject.getClass()));
			}
			Transponder transponder = Transponder.getTransponder(thisObject);
			transponder.getDriver().command(command[1], command[0], params, origin.getGenericReturnType());
		}
	}
	
	/**
	 * ByteBuddy delegate for non-void {@link Command} processing
	 */
	public static class CommandDelegate {
		/**
		 * ByteBuddy delegate
		 * @param command already translated query for the command
		 * @param origin original method to support dynamic casting
		 * @param thisObject wrapper object
		 * @param args array with all arguments
		 * @return result of the command
		 */
		@RuntimeType
		public static Object executeCommand(@QueryValue String[] command, @Origin Method origin, @This Object thisObject, @AllArguments Object[] args) {
			Map<String, Object> params = toArguments(origin, args);
			if(thisObject instanceof ITransponderEntity) {
				params.put("target", Transponder.unwrap(thisObject));
				params.put("targetType", resolveEntityType(thisObject.getClass()));
			}
			Transponder transponder = Transponder.getTransponder(thisObject);
			Object ret = transponder.getDriver().command(command[1], command[0], params, origin.getGenericReturnType());
			return transponder.wrap(ret, origin.getGenericReturnType());
		}
	}

}
