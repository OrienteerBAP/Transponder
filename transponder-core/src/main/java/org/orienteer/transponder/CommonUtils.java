package org.orienteer.transponder;

import static com.google.common.primitives.Primitives.wrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;


import lombok.experimental.UtilityClass;

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
    
    public Class<?> typeToRequiredClass(Type type, Class<?> parentClass) {
		return typeToRequiredClass(type, parentClass==null?false:Map.class.isAssignableFrom(parentClass));
	}
	
	private Class<?> typeToRequiredClass(Type type, boolean isParentMap) {
		if(type instanceof Class) return wrap((Class<?>) type);
		else if(type instanceof WildcardType) 
			return typeToRequiredClass(((WildcardType)type).getUpperBounds()[0], false);
		else if(type instanceof ParameterizedType)
			return typeToRequiredClass(((ParameterizedType)type).getActualTypeArguments()[isParentMap?1:0], false);
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

}
