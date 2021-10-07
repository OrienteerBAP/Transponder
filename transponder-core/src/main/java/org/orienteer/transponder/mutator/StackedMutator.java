package org.orienteer.transponder.mutator;

import java.util.Arrays;
import java.util.List;

import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;

import net.bytebuddy.dynamic.DynamicType.Builder;

public class StackedMutator implements IMutator {
	
	public static final IMutator DAO_MUTATOR = new StackedMutator(new QueryMutator());
	public static final IMutator ENTITY_MUTATOR = new StackedMutator(new GetterMutator(),
																	 new SetterMutator(),
																	 new QueryMutator());
	
	private final List<IMutator> stack;
	
	public StackedMutator(IMutator... stack) {
		this(Arrays.asList(stack));
	}
	
	public StackedMutator(List<IMutator> stack) {
		this.stack = stack;
	}

	@Override
	public <T> Builder<T> mutate(Transponder transponder, Builder<T> builder) {
		Builder<T> ret = builder;
		for (IMutator mutator : stack) {
			ret = mutator.mutate(transponder, ret);
		}
		return ret;
	}
	
	public static IMutator resolveRootMutator(boolean entity) {
		return entity?ENTITY_MUTATOR:DAO_MUTATOR;
	}
	
	
}
