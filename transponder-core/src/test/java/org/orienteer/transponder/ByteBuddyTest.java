package org.orienteer.transponder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.orienteer.transponder.CommonUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.Advice.Return;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.field.FieldDescription.InDefinedShape;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodCall.ArgumentLoader;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.pool.TypePool;

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
	
	//-------------------------------------------
	
	@Test
	public void testInflightInterceptor() throws Exception {
		DynamicType.Loaded<?> loaded = new ByteBuddy()
                .subclass(Object.class)
                .implement(IEchoInt.class)
                .method(isDeclaredBy(IEchoInt.class).and(isAbstract()))
                .intercept(MethodDelegation.to(EchoDelegator.class))
                .make()
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER);
		IEchoInt obj =  (IEchoInt)loaded.getLoaded().newInstance();
		assertEquals(3, obj.echoInt(3));
		assertEquals(10, obj.echoInt(10));
		
		DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(Object.class)
                .implement(IEchoInt.class);
        builder = builder.method(isDeclaredBy(IEchoInt.class).and(isAbstract()))
                .intercept(Advice.to(AdviceToIncrement.class).wrap(MethodDelegation.to(EchoDelegator.class)));
		
		DynamicType.Loaded<?> loaded2 = builder
										.make()
										.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER);
		
		
		obj =  (IEchoInt)loaded2.getLoaded().newInstance();
		assertEquals(4, obj.echoInt(3));
		assertEquals(11, obj.echoInt(10));
	}
	
	public static interface IEchoInt {
		@Test
		public int echoInt(int number);
	}
	
	public static class EchoDelegator {
		public static int implementEcho(@Argument(0) int number) {
			return number;
		}
	}
	
	public static class AdviceToIncrement {
		
		@Advice.OnMethodEnter
		public static void justPrint(@net.bytebuddy.asm.Advice.Origin Method m) {
			System.out.println("Entering "+m);
		}
		
		@Advice.OnMethodExit(inline = true)
		public static void increase(@Return(readOnly = false) int ret, @net.bytebuddy.asm.Advice.Origin Method m) {
			System.out.println("Exiting "+m);
			ret++;
		}
	}
}
