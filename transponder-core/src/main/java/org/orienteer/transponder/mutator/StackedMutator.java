package org.orienteer.transponder.mutator;

import java.util.Arrays;
import java.util.List;

import org.orienteer.transponder.BuilderScheduler;
import org.orienteer.transponder.IMutator;
import org.orienteer.transponder.Transponder;
import org.orienteer.transponder.annotation.DefaultValue;

import net.bytebuddy.dynamic.DynamicType.Builder;

/**
 * {@link IMutator} which uses stack of other mutators
 */
public class StackedMutator implements IMutator {
	
	/**
	 * Predefined mutators for DAO classes
	 */
	public static final IMutator DAO_MUTATOR 	= new StackedMutator(new AnnotationMutator(DefaultValue.class),
																	 new CommandMutator(),
			 														 new LookupMutator(), 
																     new QueryMutator());
	/**
	 * Predefined mutators for entity/wrapper classes
	 */
	public static final IMutator ENTITY_MUTATOR = new StackedMutator(new AnnotationMutator(DefaultValue.class),
																	 new CommandMutator(),
																	 new GetterMutator(),
																	 new SetterMutator(),
																	 new LookupMutator(),
																	 new QueryMutator());
	
	private final List<IMutator> stack;
	
	/**
	 * Creates {@link StackedMutator} from the stack of other mutators
	 * @param stack set of other mutators
	 */
	public StackedMutator(IMutator... stack) {
		this(Arrays.asList(stack));
	}
	
	/**
	 * Creates {@link StackedMutator} from the stack of other mutators
	 * @param stack set of other mutators
	 */
	public StackedMutator(List<IMutator> stack) {
		this.stack = stack;
	}

	@Override
	public <T> Builder<T> mutate(Transponder transponder, Builder<T> builder, BuilderScheduler scheduler) {
		Builder<T> ret = builder;
		for (IMutator mutator : stack) {
			ret = mutator.mutate(transponder, ret, scheduler);
		}
		return ret;
	}
	
	/**
	 * Resolve mutator according to nature of generated class
	 * @param entity is class going to be used for entity/wrappers?
	 * @return corresponding mutator: either {@link #DAO_MUTATOR} or {@link #ENTITY_MUTATOR}
	 */
	public static IMutator resolveRootMutator(boolean entity) {
		return entity?ENTITY_MUTATOR:DAO_MUTATOR;
	}
	
	
}
