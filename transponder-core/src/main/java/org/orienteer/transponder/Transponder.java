package org.orienteer.transponder;

import static org.orienteer.transponder.CommonUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.orienteer.transponder.annotation.EntityProperty;
import org.orienteer.transponder.annotation.EntityType;
import org.orienteer.transponder.mutator.StackedMutator;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

public class Transponder {
	
	private static final Class<?>[] NO_CLASSES = new Class<?>[0];
	
	private final IDriver driver;
	
	private static final TypeCache<Integer> DAO_CACHE = new TypeCache<Integer>(TypeCache.Sort.SOFT);
	
	public Transponder(IDriver driver) {
		this.driver = driver;
	}
	
	public IDriver getDriver() {
		return driver;
	}
	
	public <T> T create(Class<T> mainClass, Class<?>... additionalInterfaces) {
		EntityType entityType = mainClass.getAnnotation(EntityType.class);
		return create(mainClass, entityType.value(), additionalInterfaces);
	}
	
	public <T> T create(Class<T> mainClass, String className, Class<?>... additionalInterfaces) {
		if(className==null) throw new NullPointerException("ClassName for Transponder.create(...) should not be null");
		Class<T> proxyClass = getProxyClass(driver.getEntityBaseClass(), mainClass, StackedMutator.ENTITY_MUTATOR, additionalInterfaces);
		return driver.newEntityInstance(proxyClass, className);
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Class<T> getProxyClass(Class<?> baseClass, Class<T> mainClass, IMutator rootMutator, final Class<?>... additionalInterfaces) {
		Integer hash =   Arrays.hashCode(additionalInterfaces);
		hash = Objects.hashCode(driver.getCacheKey(), mainClass, hash);
		return (Class<T>) DAO_CACHE.findOrInsert(mainClass.getClassLoader(), hash, () -> {
			ByteBuddy byteBuddy = new ByteBuddy();
			DynamicType.Builder<T> builder;
			if(!mainClass.isInterface()) {
				if(!baseClass.isAssignableFrom(mainClass))
					throw new IllegalArgumentException("Class "+mainClass.getName()+" should be inherited from "+baseClass.getName());
				builder = byteBuddy.subclass(mainClass);
				if(additionalInterfaces!=null && additionalInterfaces.length>0) 
					builder = builder.implement(additionalInterfaces);
			} else {
				Class<?>[] interfaces = new Class[1+additionalInterfaces.length];
				interfaces[0] = mainClass;
				builder = (DynamicType.Builder<T>) byteBuddy
						.subclass(baseClass)
						.implement(interfaces);
			}
			if(rootMutator!=null) builder = rootMutator.mutate(this, builder);
			return builder.make()
					  .load(mainClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
					  .getLoaded();
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> T dao(Class<T> mainClass, final Class<?>... additionalInterfaces) {
		return driver.newDAOInstance(getProxyClass(Object.class, mainClass, StackedMutator.DAO_MUTATOR, additionalInterfaces));
	}
	
	public Transponder describe(Class<?>... classes) {
		DescribeContext ctx = new DescribeContext(this);
		describe(Arrays.asList(classes), ctx);
		ctx.close(false);
		return this;
	}
	
	private Set<String> describe(List<Class<?>> classes, DescribeContext ctx) {
		Set<String> types = new HashSet<String>();
		for (Class<?> clazz : classes) {
			String className = describe(clazz, ctx);
			if(className!=null) types.add(className);
		}
		return types;
	}
	
	String describe(Class<?> clazz, DescribeContext ctx) {
		if(clazz==null || !clazz.isInterface()) return null;	
		EntityType type = clazz.getAnnotation(EntityType.class);
		if(type==null) return null;
		if(ctx.wasDescribed(clazz)) return ctx.getType(clazz);
		ctx.entering(clazz, type.value());
		List<Class<?>> interfaces = Arrays.asList(clazz.getInterfaces());
		Set<String> superClasses = describe(interfaces, ctx);
		superClasses.addAll(Arrays.asList(type.superTypes()));
		
		int currentOrder=0;
		
		List<Method> methods = listMethods(clazz);
		
		for(Method method : methods) {
			if(method.isDefault() || Modifier.isStatic(method.getModifiers())) continue; //Ignore default methods
			String methodName = method.getName();
			Parameter[] params =  method.getParameters();
			String fieldNameCandidate = null;
			final Class<?> javaType;
			Class<?> subJavaTypeCandidate = null;
			if(methodName.startsWith("set") && params.length==1) {
				fieldNameCandidate = decapitalize(methodName.substring(3));
				javaType = params[0].getType();
				subJavaTypeCandidate = typeToRequiredClass(params[0].getParameterizedType(), javaType);
			} else if(methodName.startsWith("get") && params.length==0) {
				fieldNameCandidate = decapitalize(methodName.substring(3));
				javaType = method.getReturnType();
				subJavaTypeCandidate = typeToRequiredClass(method.getGenericReturnType(), javaType);
			} else if(methodName.startsWith("is") && params.length==0) {
				fieldNameCandidate = decapitalize(methodName.substring(2));
				javaType = method.getReturnType();
				subJavaTypeCandidate = typeToRequiredClass(method.getGenericReturnType(), javaType);
			} else continue;
			if(subJavaTypeCandidate!=null && subJavaTypeCandidate.equals(javaType)) subJavaTypeCandidate = null;
			final Class<?> subJavaType = subJavaTypeCandidate;
			final EntityProperty property = method.getAnnotation(EntityProperty.class);
			if(property!=null && !Strings.isNullOrEmpty(property.value())) fieldNameCandidate = property.value();
			final boolean wasPreviouslyScheduled = ctx.isPropertyCreationScheduled(fieldNameCandidate);
			//Skip second+ attempt to create a property, except if @EntityProperty is present
			if(wasPreviouslyScheduled /*&& canSkipIfAlreadyScheduled(property)*/) continue;
			
			String linkedTypeCandidate = ctx.resolveOrDescribeTypeClass(javaType);
			if(linkedTypeCandidate==null) linkedTypeCandidate = ctx.resolveOrDescribeTypeClass(subJavaType);
			/*String linkedClassCandidate = ctx.resolveOrDescribeOClass(helper, subJavaType);
			if(linkedClassCandidate==null) linkedClassCandidate = ctx.resolveOrDescribeOClass(helper, javaType);*/
			if(linkedTypeCandidate==null && property!=null && !Strings.isNullOrEmpty(property.linkedType())) linkedTypeCandidate = property.linkedType();
			
			final String fieldName = fieldNameCandidate;
			final String linkedType = linkedTypeCandidate;
			final int order = currentOrder++;
			
			ctx.postponeTillExit(fieldName, () -> {
				driver.createProperty(ctx.getCurrentType(), fieldName, linkedType, order);
			});
			if(linkedType!=null && !wasPreviouslyScheduled) ctx.postponeTillDefined(linkedType, () -> {
				String inverse = property!=null?Strings.emptyToNull(property.inverse()):null;
				driver.setupRelationship(type.value(), fieldName, linkedType, inverse);
			});
		}
		
		driver.createType(type.value(), type.isAbstract(), superClasses.toArray(new String[superClasses.size()]));
		
		ctx.exiting(clazz, type.value());
		return type.value();
	}
	
	/*private static boolean canSkipIfAlreadyScheduled(EntityProperty entityProperty) {
		if(entityProperty==null) return true;
		Set<String> difference = CommonUtils.diffAnnotations(entityProperty, DEFAULT_DAOFIELD);
		return difference.size()==1 && difference.contains("value");
	}*/
	
	static List<Method> listMethods(Class<?> clazz) {
		Method[] unsortedMethods = clazz.getDeclaredMethods();
		Map<String, Method> methodMapping = new HashMap<>();
		for (Method method : unsortedMethods) {
			methodMapping.put(method.getName()+Type.getMethodDescriptor(method), method);
		}
		//Sort by line number, but if no info: give priority for methods with DAOField annotation
		List<Method> sortedMethods = new ArrayList<Method>(unsortedMethods.length);
		try(InputStream in = clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class")) {
	      if (in != null) {
	          new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM7) {
	        	  @Override
		        	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
		        			String[] exceptions) {
	        		  Method methodToAdd = methodMapping.get(name+descriptor);
	        		  if(methodToAdd!=null) sortedMethods.add(methodToAdd);
	        		  return null;
		        	}
				}, ClassReader.SKIP_FRAMES);
	          return sortedMethods;
	      }
		} catch (IOException exc) {
		}
		//Ubnormal termination: so lets return original order
		return Arrays.asList(unsortedMethods);
		
	}
	
}
