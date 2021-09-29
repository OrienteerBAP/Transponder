package org.orienteer.transponder;

import net.bytebuddy.dynamic.DynamicType;

public interface IMutator {
	public <T> DynamicType.Builder<T> mutate(Transponder transponder, DynamicType.Builder<T> builder);
}
