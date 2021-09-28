package org.orienteer.transponder.mutator;

import java.util.Arrays;
import java.util.List;

import org.orienteer.transponder.IMutator;

import net.bytebuddy.dynamic.DynamicType.Builder;

public class StackedMutator implements IMutator {
	
	private final List<IMutator> stack;
	
	public StackedMutator(IMutator... stack) {
		this(Arrays.asList(stack));
	}
	
	public StackedMutator(List<IMutator> stack) {
		this.stack = stack;
	}

	@Override
	public <T> Builder<T> mutate(Builder<T> builder) {
		Builder<T> ret = builder;
		for (IMutator mutator : stack) {
			ret = mutator.mutate(ret);
		}
		return ret;
	}
	
	
}
