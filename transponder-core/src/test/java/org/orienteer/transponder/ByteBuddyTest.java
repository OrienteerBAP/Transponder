package org.orienteer.transponder;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.orienteer.transponder.CommonUtils;
import org.orienteer.transponder.Transponder.ITransponderHolder;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.Advice.Return;
import net.bytebuddy.asm.MemberSubstitution;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.field.FieldDescription.InDefinedShape;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodCall.ArgumentLoader;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.DefaultMethodCall;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
	
	@Test
	public void testDelegation() throws Exception {
		DelegateMe obj = new DelegateMe(100);
		assertEquals(100, obj.getNumber());
		assertEquals(101, obj.getNumberPlusOneWithMethod());
		assertEquals(101, obj.getNumberPlusOneDirect());
		
		DynamicType.Loaded<?> loaded = new ByteBuddy()
                .subclass(DelegateMe.class)
                .implement(DelegateInterface.class)
                .method(new InterceptorEM( isAccessibleTo(DelegateMe.class).and(CommonUtils.isSimiliarToMethodIn(DelegateInterface.class))))
                .intercept(DefaultMethodCall.prioritize(DelegateInterface.class))
                .make()
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER);
		DelegateMe delegator =  (DelegateMe)loaded.getLoaded().newInstance();
		assertEquals(1, delegator.getNumber());
		assertEquals(2, delegator.getNumberPlusOneWithMethod());
		assertEquals(1, delegator.getNumberPlusOneDirect());
	}
	
	@Test
	public void testDelegation2() throws Exception {
		DelegateMe obj = new DelegateMe(100);
//		assertEquals(100, obj.getNumber());
//		assertEquals(101, obj.getNumberPlusOneWithMethod());
		assertEquals(101, obj.getNumberPlusOneDirect());
		
		DynamicType.Loaded<?> loaded = new ByteBuddy()
                .subclass(obj.getClass())
                .implement(IDelegateAware.class)
                .method(not(isNative()))
                .intercept(MethodCall.invokeSelf().onMethodCall(MethodCall.invoke(named("get$delegate")))
                		  .withAllArguments()
                		  .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
                .defineField("$delegate", Object.class, Opcodes.ACC_PRIVATE)
				.method(isDeclaredBy(IDelegateAware.class)
							.and(isAbstract()))
				.intercept(FieldAccessor.ofField("$delegate"))
                .make()
                .load(getClass().getClassLoader());
		
		DelegateMe delegator =  (DelegateMe)loaded.getLoaded().newInstance();
		((IDelegateAware)delegator).set$delegate(obj);
		assertTrue(obj==((IDelegateAware)delegator).get$delegate());
		System.out.println("Number: "+delegator.getNumber());
		assertEquals(100, obj.getNumber());
		assertEquals(100, delegator.getNumber());
		assertEquals(101, delegator.getNumberPlusOneWithMethod());
		assertEquals(101, delegator.getNumberPlusOneDirect());
	}
	
	private static class InterceptorEM extends ElementMatcher.Junction.AbstractBase<MethodDescription> {
		
		private final ElementMatcher<MethodDescription> delegate;
		
		public InterceptorEM(ElementMatcher<MethodDescription> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean matches(MethodDescription target) {
			System.out.println("Analyzing: "+target);
			target.getDeclaredAnnotations().forEach(ad -> System.out.println("Annotation:"+ad));
			return delegate.matches(target);
		}
		
	}
	
	public static interface IDelegateAware {
		public Object get$delegate();
		public void set$delegate(Object delegate);
	}
	
	public static class DelegateMe {
		
		private int number;
		
		public DelegateMe() {
			this(0);
		}
		
		public DelegateMe(int number) {
			this.number = number;
		}
		
		public int getNumber() {
			System.out.println("GetNumber Invoked! Number is "+number);
			System.out.println("This: "+this);
			return number;
		}
		
		public int getNumberPlusOneWithMethod() {
			return getNumber()+1;
		}
		
		public int getNumberPlusOneDirect() {
			return number+1;
		}
	}
	
	@Retention(RUNTIME)
	@Target({METHOD})
	@Inherited
	public static @interface OvverideByMe {
		
	}
	
	public static interface DelegateInterface {
		
		@OvverideByMe
		public default int getNumber() {
			System.out.println("Invoked!");
			return 1;
		}
		
		@OvverideByMe
		public default int getNumber2() {
			System.out.println("Invoked!");
			return 1;
		}
		
	}
}
