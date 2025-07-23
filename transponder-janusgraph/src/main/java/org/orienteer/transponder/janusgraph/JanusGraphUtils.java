package org.orienteer.transponder.janusgraph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.orienteer.transponder.Transponder;

/**
 * Utility class for JanusGraph specific operations
 */
public class JanusGraphUtils {
	
	private static final Set<Class<?>> SUPPORTED_PROPERTY_CLASSES = new HashSet<>(Arrays.asList(
			String.class, Integer.class, int.class, Long.class, long.class,
			Double.class, double.class, Float.class, float.class,
			Boolean.class, boolean.class, Character.class, char.class,
			Byte.class, byte.class, Short.class, short.class
	));

	/**
	 * Check if provided class is supported as property by JanusGraph
	 * @param clazz class to check
	 * @return true if class can be stored as JanusGraph property
	 */
	public static boolean isSupportedPropertyClass(Class<?> clazz) {
		return SUPPORTED_PROPERTY_CLASSES.contains(clazz);
	}
	
	/**
	 * Get expected TinkerPop Element class for given wrapper class
	 * @param wrapperClass class to analyze
	 * @return Vertex.class, Edge.class or null if not a graph element wrapper
	 */
	public static Class<? extends Element> getExpectedElementClass(Class<?> wrapperClass) {
		if(Transponder.ITransponderHolder.class.isAssignableFrom(wrapperClass)) {
			// This is a wrapper - determine if it wraps Vertex or Edge
			// For now, assume all wrappers are Vertices unless specified otherwise
			return Vertex.class;
		}
		
		if(Vertex.class.isAssignableFrom(wrapperClass)) return Vertex.class;
		if(Edge.class.isAssignableFrom(wrapperClass)) return Edge.class;
		
		return null;
	}
	
	/**
	 * Convert vertex to its label (type)
	 * @param vertex vertex to get label from
	 * @return vertex label or null
	 */
	public static String vertexToType(Vertex vertex) {
		if(vertex != null) {
			return vertex.label();
		}
		return null;
	}
	
	/**
	 * Convert element to list of types/labels
	 * @param element element to get labels from
	 * @return list of labels
	 */
	public static List<String> elementToType(Element element) {
		if(element != null) {
			return Arrays.asList(element.label());
		}
		return Arrays.asList();
	}
}