package org.orienteer.transponder.mutator;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.CommonUtils;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.annotation.Command;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.jar.asm.Opcodes;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * {@link IMutator} and delegate to implement methods from provided delegate
 */
public class DelegatorMutator implements IMutator {

	@Override
	public <T> Builder<T> mutate(Transponder transponder, Builder<T> builder, BuilderScheduler scheduler) {
		builder = builder.defineField("$delegate", Object.class, Opcodes.ACC_PRIVATE)
			.method(isDeclaredBy(Transponder.ITransponderDelegator.class)
					.and(isAbstract()))
			.intercept(FieldAccessor.ofField("$delegate"));
		TypeDescription type = builder.toTypeDescription();
		Generic superClass = type.getSuperClass();
		scheduler.schedule(not(isNative().or(isPrivate()).or(isConstructor()).or(isStatic())).and(CommonUtils.isMethodPresent(superClass.asErasure())), 
				MethodCall.invokeSelf().onMethodCall(MethodCall.invoke(named("get$delegate")))
        		  .withAllArguments()
        		  .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));
		return builder;
	}
}
