package org.orienteer.transponder.mutator;

import static net.bytebuddy.matcher.ElementMatchers.*;

import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;

import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;

public class SetterMutator implements IMutator {

	@Override
	public <T> Builder<T> mutate(Transponder transponder, Builder<T> builder) {
		return builder.method(isSetter().and(isAbstract()))
				.intercept(MethodDelegation.to(transponder.getDriver().getSetterDelegationClass()));
	}

}
