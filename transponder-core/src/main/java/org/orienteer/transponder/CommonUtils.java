package org.orienteer.transponder;

import static com.google.common.primitives.Primitives.wrap;
import static net.bytebuddy.matcher.ElementMatchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.orienteer.transponder.Transponder.ITransponderHolder;

import com.google.common.base.Strings;

import lombok.experimental.UtilityClass;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * Common for utility methods
 */
@UtilityClass
public class CommonUtils {

	/**
	 * Converts given objects into map.
	 * Uses pairs of object.
	 * Call toMap("key1", "value1", "key2", "value2") will returns this map:
	 * { "key1": "value1", "key2": "value2" }
	 * Call method with not pair arguments will throw {@link IllegalStateException}.
	 * For example: toMap("key1", "value1", "key2") - throws {@link IllegalStateException}
	 * @param objects {@link Object[]} array of objects which will be used for create new map
	 * @param <K> type of map key
	 * @param <V> type of map value
	 * @return {@link Map} created from objects
	 * @throws IllegalStateException if objects are not pair
	 */
	public <K, V> Map<K, V> toMap(Object... objects) {
		if(objects==null || objects.length % 2 !=0) throw new IllegalArgumentException("Illegal arguments provided to construct a map");
		Map<K, V> ret = new HashMap<K, V>();
		for(int i=0; i<objects.length; i+=2) {
			ret.put((K)objects[i], (V)objects[i+1]);
		}
		return ret;
	}
	
	/**
	 * Check if given collection is not empty
	 * @param collection collection to test
	 * @param <T> type of collection
	 * @return true if collection is not empty
	 */
	public <T> boolean isNotEmpty(Collection<T> collection) {
		return collection != null && !collection.isEmpty();
	}

	/**
	 * Return main object if it's not null or default
	 * @param <T> required return type
	 * @param object main object
	 * @param def default object
	 * @return main object if it's not null or default
	 */
	public <T> T defaultIfNull(T object, T def) {
		return object!=null?object:def;
	}
	
	/**
	 * Returns defaultValue if actual value is null or empty.
	 * @param value value to check
	 * @param defaultValue value to return if main value is null or empty
	 * @return value if it's not null or empty or defaultValue
	 */
	public String defaultIfNullOrEmpty(String value, String defaultValue) {
		return !Strings.isNullOrEmpty(value)?value:defaultValue;
	}
	
	/**
	 * Return main object if it's not null or supplied default
	 * @param <T> required return type
	 * @param object main object
	 * @param supplier supplier of default object
	 * @return main object if it's not null or supplied default
	 */
	public <T> T defaultIfNull(T object, Supplier<T> supplier) {
		return object!=null?object:supplier.get();
	}
	
	/**
	 * Combine array of {@link Optional} and return first not empty
	 * @param <T> type of required {@link Optional} 
	 * @param optionals array of {@link Optional}s
	 * @return first not empty {@link Optional}
	 */
	public <T> Optional<T> orOptional(Optional<T>... optionals) {
		for (Optional<T> optional : optionals) {
			if(optional.isPresent()) return optional;
		}
		return Optional.empty();
	}
	
	/**
	 * Safe method to merge sets. Always return not null
	 * @param <T> type of sets and required result
	 * @param mainSet main set to merge into
	 * @param mergeSet set to merge
	 * @return mergedSet - not null
	 */
	public <T> Set<T> mergeSets(Set<T> mainSet, Collection<T> mergeSet) {
		if(mainSet==null) mainSet = new HashSet<>();
		if(mergeSet!=null) mainSet.addAll(mergeSet);
		return mainSet;
	}
	
	/**
	 * Safe method to merge maps
	 * @param <K> type of keys in map
	 * @param <V> type of values in map
	 * @param mainMap main map to merge into
	 * @param mergeMap map to merge
	 * @return merged map - not null
	 */
	public <K, V> Map<K,V> mergeMaps(Map<K, V> mainMap, Map<K,V> mergeMap) {
		if(mainMap==null) mainMap = new HashMap<>();
		if(mergeMap!=null) mainMap.putAll(mergeMap);
		return mainMap;
	}
	
	/**
	 * Capitalizes a string.
	 * 
	 * @param s
	 *            The string
	 * @return The capitalized string
	 */
	public String capitalize(final String s) {
		if(s==null || s.isEmpty()) return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
	
	/**
	 * DeCapitalizes a string.
	 * 
	 * @param s
	 *            The string
	 * @return The de-capitalized string
	 */
	public String decapitalize(final String s)
	{
		if(s==null || s.isEmpty()) return s;
		return Character.toLowerCase(s.charAt(0)) + s.substring(1);
	}
	
    public <A extends Annotation> Set<String> diffAnnotations(A ann1, A ann2){
    	try {
			Set<Method> methods = new HashSet<Method>(Arrays.asList(ann1.getClass().getInterfaces()[0].getMethods()));
			methods.removeAll(Arrays.asList(Annotation.class.getMethods()));
			Set<String> diff = new HashSet<String>();
			for (Method method : methods) {
				if(method.getParameterTypes().length==0
						&& !Objects.equals(method.invoke(ann1), method.invoke(ann2))) {
					diff.add(method.getName());
				}
			}
			return diff;
		} catch (Exception e) {
			throw new IllegalStateException("Can't calculat diff due to exception", e);
		} 
    }
    
    public Annotation getFirstPresentAnnotation(AnnotatedElement where, Class<? extends Annotation>...classes) {
    	if(classes==null || classes.length==0) return null;
    	for (Class<? extends Annotation> class1 : classes) {
    		Annotation annotation = where.getAnnotation(class1);
    		if(annotation!=null) return annotation;
		}
    	return null;
    }
    
    public Class<?> typeToMasterClass(Type type) {
    	
    	if(type instanceof Class) return wrap((Class<?>)type);
    	else if(type instanceof ParameterizedType)
			return typeToMasterClass(((ParameterizedType)type).getRawType());
    	return null;
    }
    
	public Class<?> typeToRequiredClass(Type type) {
		if(type instanceof Class) return wrap((Class<?>) type);
		else if(type instanceof WildcardType) 
			return typeToRequiredClass(((WildcardType)type).getUpperBounds()[0]);
		else if(type instanceof ParameterizedType) {
			boolean isParentMap = Map.class.isAssignableFrom(typeToMasterClass(type));
			return typeToRequiredClass(((ParameterizedType)type).getActualTypeArguments()[isParentMap?1:0]);
		}
		return null;
	}
	
	public <T> T newInstance(Class<T> clazz) {
		if(!clazz.isInterface()) {
			try {
				return (T) clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalArgumentException("Can't instantiate "+clazz.getName(), e);
			} 
		}
		else if(clazz.isAssignableFrom(ArrayList.class)) return (T) new ArrayList<>();
		else if(clazz.isAssignableFrom(HashSet.class)) return (T) new HashSet<>();
		else if(clazz.isAssignableFrom(HashMap.class)) return (T) new HashMap<>();
		else throw new IllegalArgumentException("Can't instantiate "+clazz.getName());
	}
	
	public boolean isSimpleType(final Object iObject) {
		if (iObject == null)
			return false;

		final Class<? extends Object> iType = iObject.getClass();

		if (iType.isPrimitive() || Number.class.isAssignableFrom(iType) || String.class.isAssignableFrom(iType)
				|| Boolean.class.isAssignableFrom(iType) || Date.class.isAssignableFrom(iType)
				|| (iType.isArray() && (iType.equals(byte[].class) || iType.equals(char[].class)
						|| iType.equals(int[].class) || iType.equals(long[].class) || iType.equals(double[].class)
						|| iType.equals(float[].class) || iType.equals(short[].class) || iType.equals(Integer[].class)
						|| iType.equals(String[].class) || iType.equals(Long[].class) || iType.equals(Short[].class)
						|| iType.equals(Double[].class))))
			return true;

		return false;
	}
	
	public Map<String, Object> toArguments(Method method, Object[] values) {
		return toArguments(null, true, method, values);
	}
	
	public Map<String, Object> toArguments(Map<String, Object> args, boolean override, Method method, Object[] values) {
		if(args==null) {
			args = new HashMap<>();
			override = true;
		}
		
		Parameter[] params = method.getParameters();
		for (int i = 0; i < params.length; i++) {
			Object value = Transponder.unwrap(values[i]);
			if(override) {
				args.put(params[i].getName(), value);
				args.put("arg"+i, value);
			}
			else {
				args.putIfAbsent(params[i].getName(), value);
				args.putIfAbsent("arg"+i, value);
			}
		}
		return args;
	}
	
	public String interpolate(String pattern, Map<String, Object> params) {
		String ret = pattern;
		if(params!=null && !params.isEmpty()) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				String key = entry.getKey();
				Object val = entry.getValue();
				ret = ret.replace("${"+key+"}", Objects.toString(val, ""));
			}
		}
		return ret;
	}
	
	public List<Method> listDeclaredMethods(Class<?> clazz) {
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
	
	public <T> boolean hasMatch(Iterable<T> iterable, ElementMatcher<? super T> matcher)
	{
		for (T t : iterable) {
			if(matcher.matches(t)) return true;
		}
		return false;
	}
	
	public List<MethodDescription> getMethodDescriptionList(TypeDefinition type) {
		List<MethodDescription> ret = new ArrayList<>();
		collectMethodDescriptions(type, ret);
		return ret;
	}
	
	public void collectMethodDescriptions(TypeDefinition type, List<MethodDescription> list) {
		if(type==null) return;
		list.addAll(type.getDeclaredMethods());
		collectMethodDescriptions(type.getSuperClass(), list);
		TypeList.Generic interfaces = type.getInterfaces();
		interfaces.forEach(intf -> collectMethodDescriptions(intf, list));
	}
	
	public List<Method> getMethodList(Class<?> clazz) {
		List<Method> ret = new ArrayList<>();
		collectMethods(clazz, ret);
		return ret;
	}
	
	public void collectMethods(Class<?> clazz, List<Method> list) {
		if(clazz==null) return;
		list.addAll(Arrays.asList(clazz.getDeclaredMethods()));
		Class<?> superClass = clazz.getSuperclass();
		collectMethods(superClass, list);
		Class<?>[] interfaces = clazz.getInterfaces();
		for (Class<?> intf : interfaces) {
			collectMethods(intf, list);
		}
	}
	
}
