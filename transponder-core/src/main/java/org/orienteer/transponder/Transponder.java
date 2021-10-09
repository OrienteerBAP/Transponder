package org.orienteer.transponder;

import static org.orienteer.transponder.CommonUtils.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

public class Transponder {
	
	private static final Class<?>[] NO_CLASSES = new Class<?>[0];
	
	private final IDriver driver;
	
	private static final TypeCache<Integer> DAO_CACHE = new TypeCache<Integer>(TypeCache.Sort.SOFT);
	
	public static interface ITransponderHolder {
		public Transponder get$transponder();
		public void set$transponder(Transponder transponder);
	}
	
	public static interface ITransponderEntity extends ITransponderHolder{
		
	}
	
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
		Class<T> proxyClass = getProxyClass(driver.getDefaultEntityBaseClass(), mainClass, true, additionalInterfaces);
		return setTransponder(driver.newEntityInstance(proxyClass, className));
	}
	
	public <T> T provide(Object object) {
		Class<T> mainClass = (Class<T>)driver.getEntityMainClass(object);
		return provide(object, mainClass);
	}
	
	public <T> T provide(Object object, Class<T> mainClass, Class<?>... additionalInterfaces) {
		Class<T> expectedMainClass = (Class<T>)driver.getEntityMainClass(object);
		if(expectedMainClass!=null && mainClass.isAssignableFrom(expectedMainClass)) mainClass = expectedMainClass;
		if(mainClass.isInstance(object)) {
			boolean compatible = true;
			if(additionalInterfaces!=null) {
				for (Class<?> addon : additionalInterfaces) {
					if(!addon.isInstance(object)) {
						compatible = false;
						break;
					}
				}
			}
			if(compatible) return (T) object;
		}
		Class<T> proxyClass = getProxyClass(driver.getDefaultEntityBaseClass(), mainClass, true, additionalInterfaces);
		return setTransponder(driver.wrapEntityInstance(proxyClass, object));
	}
	
	public <T> T wrap(Object seed, Type targetType) {
		if(seed==null) return null;
		Class<?> requiredClass = CommonUtils.typeToMasterClass(targetType);
		if(seed instanceof ITransponderHolder) {
			return (T) seed;
		} else if(driver.isSeed(seed)) {
			return (T) provide(seed, requiredClass);
		} else if(seed instanceof Iterable) {
			Iterator<?> it = ((Iterable<?>)seed).iterator(); 
			if(!it.hasNext()) return (T) newInstance(requiredClass);
			Object probe;
			do {
				probe = it.next();
			} while(it.hasNext() && probe == null);
			if(driver.isSeed(probe)) {
				return wrapIterable((Iterable<?>)seed, targetType);
			} else if(Collection.class.isAssignableFrom(requiredClass)) {
				Collection<Object> collection = (Collection<Object>)newInstance(requiredClass);
				if(probe!=null) collection.addAll((Collection<Object>)seed);
				else {
					Class<?> elementClass = typeToRequiredClass(targetType);
					if(elementClass==null 
							|| elementClass.getAnnotation(EntityType.class)==null) collection.addAll((Collection<Object>)seed);
				}
				return (T)collection;
			}
			else throw new IllegalStateException("Can't prepare required return class: "+requiredClass +" from "+seed.getClass());
		} else if(seed instanceof Map) {
			
			Map<?, ?> map = (Map<?, ?>)seed;
			if(map.size()==0) return (T) seed;
			Iterator<?> it = map.values().iterator();
			Object probe;
			do {
				probe = it.next();
			} while(it.hasNext() && probe == null);
			if(driver.isSeed(probe)) {
				return wrapMap((Map<?, ?>)map, targetType);
			} else if(Map.class.isAssignableFrom(requiredClass)) {
				return (T)map;
			}
			else throw new IllegalStateException("Can't prepare required return class: "+requiredClass +" from "+seed.getClass());
		} else if(requiredClass.isInstance(seed)) return (T)seed;
		return null;
	}
	
	protected <T> T wrapIterable(Iterable<?> seeds, Type targetType) {
		if(seeds==null) return null;
		Class<?> requiredSubType = typeToRequiredClass(targetType);
		
		Iterable<?> ret;
		if(driver.isSeedClass(requiredSubType)) {
			ret = seeds;
		}
		else {
			List<Object> inner = new ArrayList<>();
			for (Object seed : seeds) {
				inner.add(provide(seed, requiredSubType));
			}
			ret = inner;
		}
		
		Class<?> masterClass = CommonUtils.typeToMasterClass(targetType);
		if(masterClass.isAssignableFrom(ret.getClass())) return (T)ret;
		else if(Collection.class.isAssignableFrom(masterClass)) {
			Collection<Object> instance = (Collection<Object>)newInstance(masterClass);
			instance.addAll((Collection<Object>)ret);
			return (T) instance;
		}
		else throw new IllegalStateException("Can't prepare required return type: "+targetType);
	}
	
	protected <T> T wrapMap(Map<?, ?> map, Type targetType) {
		if(map==null) return null;
		Class<?> requiredSubType = typeToRequiredClass(targetType);
		
		Map<?, ?> ret;
		if(driver.isSeedClass(requiredSubType)) {
			ret = map;
		}
		else {
			Map<Object, Object> inner = new HashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				inner.put(entry.getKey(), provide(entry.getValue(), requiredSubType));
			}
			ret = inner;
		}
		
		if(CommonUtils.typeToMasterClass(targetType).isAssignableFrom(Map.class)) return (T) ret;
		else throw new IllegalStateException("Can't prepare required return type: "+targetType);
	}
	
	public static Object unwrap(Object arg) {
		if(arg==null) return null;
		if(isSimpleType(arg)) return arg;
		else if (arg instanceof ITransponderHolder) {
			if(arg instanceof ITransponderEntity) {
				Transponder otherTransponder = ((ITransponderHolder)arg).get$transponder();
				return otherTransponder.getDriver().toSeed(arg);
			} else return arg;
		}
		else if (arg instanceof Collection<?>) {
			Collection<?> col = (Collection<?>)arg;
			List<Object> ret = new ArrayList<>(col.size());
			for (Object object : col) ret.add(unwrap(object));
			return ret;
		} else if (arg instanceof Map) {
			Map<?, ?> map = (Map<?, ?>)arg;
			Map<Object, Object> ret = new HashMap<Object, Object>(map.size());
			for (Map.Entry<?, ?> entry : map.entrySet()) ret.put(entry.getKey(), unwrap(entry.getValue()));
			return ret;
		} else if (arg.getClass().isArray()) {
			Object[] array = (Object[])arg;
			List<Object> ret = new ArrayList<>(array.length);
			for (Object object : array) ret.add(unwrap(object));
			return ret;
		} else if(arg instanceof Serializable) {
			return arg;
		} else throw new IllegalStateException("Type "+arg.getClass()+" can't be cast to use in DB");
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Class<T> getProxyClass(Class<?> baseClass, Class<T> mainClass, boolean entity, final Class<?>... additionalInterfaces) {
		Integer hash =   Arrays.hashCode(additionalInterfaces);
		hash = Objects.hashCode(driver.getCacheKey(), mainClass, hash);
		return (Class<T>) DAO_CACHE.findOrInsert(mainClass.getClassLoader(), hash, () -> {
			ByteBuddy byteBuddy = new ByteBuddy();
			DynamicType.Builder<?> builder;
			List<Class<?>> classesToImplement = new ArrayList<>();
			
			if(!mainClass.isInterface()) {
				if(!baseClass.isAssignableFrom(mainClass))
					throw new IllegalArgumentException("Main class "+mainClass.getName()+" should be inherited from "+baseClass.getName() +" or be an interface");
				builder = byteBuddy.subclass(mainClass);
			} else {
				builder = byteBuddy.subclass(baseClass);
				classesToImplement.add(mainClass);
			}
			classesToImplement.add(entity?ITransponderEntity.class:ITransponderHolder.class);
			if(additionalInterfaces!=null && additionalInterfaces.length>0) {
				classesToImplement.addAll(Arrays.asList(additionalInterfaces));
			}
			builder = builder.implement(classesToImplement);
			builder = StackedMutator.resolveRootMutator(entity).mutate(this, builder);
			
			IMutator driverMutator = driver.getMutator();
			if(driverMutator!=null) builder = driverMutator.mutate(this, builder);
			
			builder = builder.defineField("$transponder", Transponder.class, Opcodes.ACC_PRIVATE)
									.method(isDeclaredBy(ITransponderHolder.class)
												.and(isAbstract()))
									.intercept(FieldAccessor.ofField("$transponder"));
			return builder.make()
					  .load(mainClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
					  .getLoaded();
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> T dao(Class<T> mainClass, final Class<?>... additionalInterfaces) {
		return setTransponder(driver.newDAOInstance(getProxyClass(Object.class, mainClass, false, additionalInterfaces)));
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
		final EntityType type = clazz.getAnnotation(EntityType.class);
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
			final Type fieldType;
			if(methodName.startsWith("set") && params.length==1) {
				fieldNameCandidate = decapitalize(methodName.substring(3));
				fieldType = params[0].getParameterizedType();
			} else if(methodName.startsWith("get") && params.length==0) {
				fieldNameCandidate = decapitalize(methodName.substring(3));
				fieldType = method.getGenericReturnType();
			} else if(methodName.startsWith("is") && params.length==0) {
				fieldNameCandidate = decapitalize(methodName.substring(2));
				fieldType = method.getGenericReturnType();
			} else continue;
			final EntityProperty property = method.getAnnotation(EntityProperty.class);
			if(property!=null && !Strings.isNullOrEmpty(property.value())) fieldNameCandidate = property.value();
			final boolean wasPreviouslyScheduled = ctx.isPropertyCreationScheduled(fieldNameCandidate);
			//Skip second+ attempt to create a property, except if @EntityProperty is present
			if(wasPreviouslyScheduled /*&& canSkipIfAlreadyScheduled(property)*/) continue;
			
			String linkedTypeCandidate = ctx.resolveOrDescribeTypeClass(typeToMasterClass(fieldType));
			if(linkedTypeCandidate==null) linkedTypeCandidate = ctx.resolveOrDescribeTypeClass(typeToRequiredClass(fieldType));
			if(linkedTypeCandidate==null && property!=null && !Strings.isNullOrEmpty(property.linkedType())) linkedTypeCandidate = property.linkedType();
			
			final String fieldName = fieldNameCandidate;
			final String linkedType = linkedTypeCandidate;
			final int order = currentOrder++;
			final AnnotatedElement annotations = method;
			
			ctx.postponeTillExit(fieldName, () -> {
				driver.createProperty(type.value(), fieldName, fieldType, linkedType, order, annotations);
			});
			if(linkedType!=null && !wasPreviouslyScheduled) ctx.postponeTillDefined(linkedType, () -> {
				String inverse = property!=null?Strings.emptyToNull(property.inverse()):null;
				driver.setupRelationship(type.value(), fieldName, linkedType, inverse);
			});
		}
		
		driver.createType(type.value(), type.isAbstract(), clazz, superClasses.toArray(new String[superClasses.size()]));
		
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
			methodMapping.put(method.getName()
									+net.bytebuddy.jar.asm.Type.getMethodDescriptor(method), method);
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
	
	private <T> T setTransponder(T object) {
		return setTransponder(object, this);
	}
	
	private static <T> T setTransponder(T object, Transponder transponder) {
		if(!(object instanceof ITransponderHolder)) throw new IllegalArgumentException("Object has not been provided by Transponder");
		((ITransponderHolder)object).set$transponder(transponder);
		return object;
	}
	
	public static Transponder getTransponder(Object object) {
		if(!(object instanceof ITransponderHolder)) throw new IllegalArgumentException("Object has not been provided by Transponder");
		return ((ITransponderHolder)object).get$transponder();
	}
	
}
