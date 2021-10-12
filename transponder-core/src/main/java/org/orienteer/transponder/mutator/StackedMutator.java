package org.orienteer.transponder.mutator;

import java.util.Arrays;
import java.util.List;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.annotation.DefaultValue;

import net.bytebuddy.dynamic.DynamicType.Builder;

public class StackedMutator implements IMutator {
	
	public static final IMutator DAO_MUTATOR 	= new StackedMutator(new AnnotationMutator(DefaultValue.class),
																	 new CommandMutator(),
			 														 new LookupMutator(), 
																     new QueryMutator());
	public static final IMutator ENTITY_MUTATOR = new StackedMutator(new AnnotationMutator(DefaultValue.class),
																	 new CommandMutator(),
																	 new GetterMutator(),
																	 new SetterMutator(),
																	 new LookupMutator(),
																	 new QueryMutator());
	
	private final List<IMutator> stack;
	
	public StackedMutator(IMutator... stack) {
		this(Arrays.asList(stack));
	}
	
	public StackedMutator(List<IMutator> stack) {
		this.stack = stack;
	}

	@Override
	public <T> Builder<T> mutate(Builder<T> builder, BuilderScheduler scheduler) {
		Builder<T> ret = builder;
		for (IMutator mutator : stack) {
			ret = mutator.mutate(ret, scheduler);
		}
		return ret;
	}
	
	public static IMutator resolveRootMutator(boolean entity) {
		return entity?ENTITY_MUTATOR:DAO_MUTATOR;
	}
	
	
}
