package org.orienteer.transponder.mutator;

import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;

import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class GetterMutator implements IMutator {

	@Override
	public <T> Builder<T> mutate(Transponder transponder, Builder<T> builder) {
		return builder.method(isGetter().and(isAbstract()))
				.intercept(MethodDelegation.to(transponder.getDriver().getGetterDelegationClass()));
	}

}
