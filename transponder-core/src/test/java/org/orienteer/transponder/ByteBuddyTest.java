package org.orienteer.transponder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.orienteer.transponder.CommonUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodCall.ArgumentLoader;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.junit.Assert.assertEquals;

public class ByteBuddyTest {
	
	@Test
	public void testMethodCall() throws Exception {
		 DynamicType.Loaded<?> loaded = new ByteBuddy()
	                .subclass(HashMap.class)
	                .implement(IGetterAndSetter.class)
	                .method(isGetter().and(isAbstract()))
	                .intercept(MethodDelegation.to(MapGetter.class))
	                .method(isSetter().and(isAbstract()))
	                .intercept(MethodDelegation.to(MapSetter.class))
	                .make()
	                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER);
		 IGetterAndSetter obj =  (IGetterAndSetter)loaded.getLoaded().newInstance();
		 String name = "Test Name";
		 obj.setName(name);
		 assertEquals(name, obj.getName());
	}
	
	public static class MapGetter {
		@RuntimeType
		public static Object getValue(@Origin Method method, @This Map<?, ?> thisObject) {
			String field = CommonUtils.decapitalize(method.getName().substring(3));
			return thisObject.get(field);
		}
	}
	
	public static class MapSetter {
		@RuntimeType
		public static void setValue(@Origin Method method, @This Map<Object, Object> thisObject, @Argument(0) Object value) {
			String field = CommonUtils.decapitalize(method.getName().substring(3));
			thisObject.put(field, value);
		}
	}
	
	public static interface IGetterAndSetter {
		public String getName();
		public void setName(String name);
	}
	
	//-----------------------------------------------
	
	public static class MyClass {
		public <V> V echo(V value) {
			return value;
		}
		
		public <T extends MyClass> T getThis() {
			return (T) this;
		}
	}
	
	public static interface MyInterface {
		public <V> V echo(V value);
		public default <T extends MyInterface> T getThis() {
			return (T) this;
		}
	}
	
	public static class JointMyClass extends MyClass implements MyInterface {

	}
	
	@Test
	public void testMethodsMirroring() throws Exception {
		 DynamicType.Loaded<?> loaded = new ByteBuddy()
	                .subclass(MyClass.class)
	                .implement(MyInterface.class)
	                .make()
	                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER);
		 MyInterface obj =  (MyInterface)loaded.getLoaded().newInstance();
		 assertEquals("Test", obj.echo("Test"));
		 assertEquals(obj, obj.getThis());
	}
}
