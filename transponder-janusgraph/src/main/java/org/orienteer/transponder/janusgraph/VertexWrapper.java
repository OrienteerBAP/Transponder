package org.orienteer.transponder.janusgraph;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Spliterators;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.orienteer.transponder.CommonUtils;
import org.orienteer.transponder.Transponder;

/**
 * Base class for all JanusGraph generated wrapper classes.
 * It covers TinkerPop {@link Element}, primarily Vertices
 */
public class VertexWrapper {
	protected Vertex vertex;

	/**
	 * Creates empty {@link VertexWrapper} with no associated JanusGraph vertex
	 */
	public VertexWrapper() {
	}

	/**
	 * Creates {@link VertexWrapper} and associate it with corresponding {@link Vertex}
	 * @param vertex JanusGraph vertex to bind to
	 */
	public VertexWrapper(Vertex vertex) {
		this.vertex = vertex;
	}
	
	/**
	 * @return enclosed vertex
	 */
	public Vertex getVertex() {
		return vertex;
	}
	
	/**
	 * Replace enclosed vertex with new one
	 * @param vertex new vertex
	 */
	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}
	
	/**
	 * Get a value for a {@link Transponder} property 
	 * which might be mapped to edges, vertices or actual JanusGraph properties
	 * @param property name of a property to obtain value for
	 * @param type expected return type. Used to detect proper way to access value
	 * @return value of a property
	 */
	public Object get(String property, Type type) {
		Class<?> requiredClass = CommonUtils.typeToRequiredClass(type);
		if(JanusGraphUtils.isSupportedPropertyClass(requiredClass)) return getProperty(property);
		else {
			Class<? extends Element> expectedElementClass = JanusGraphUtils.getExpectedElementClass(requiredClass);
			if(expectedElementClass==null) return null;
			else {
				if(Collection.class.isAssignableFrom(CommonUtils.typeToMasterClass(type))) 
					return getMultiReferenced(property, Vertex.class.isAssignableFrom(expectedElementClass));
				else return getSingleReferenced(property, Vertex.class.isAssignableFrom(expectedElementClass));
			}
		}
	}
	
	/**
	 * Set value for a provided property
	 * @param property name of a property to set value to
	 * @param value actual value to set
	 * @param type type of a value
	 * @return this wrapper
	 */
	public VertexWrapper set(String property, Object value, Type type) {
		Class<?> requiredClass = CommonUtils.typeToRequiredClass(type);
		if(JanusGraphUtils.isSupportedPropertyClass(requiredClass)) return setProperty(property, value);
		else {
			Class<? extends Element> expectedElementClass = JanusGraphUtils.getExpectedElementClass(requiredClass);
			if(expectedElementClass!=null) {
				if(Collection.class.isAssignableFrom(CommonUtils.typeToMasterClass(type))) 
					setMultiReferenced(property, (Collection<Element>)value, Vertex.class.isAssignableFrom(expectedElementClass));
				else setSingleReferenced(property, (Element)value, Vertex.class.isAssignableFrom(expectedElementClass));
			}
		}
		return this;
	}
	
	/**
	 * Get JanusGraph property from a vertex
	 * @param property name of a property to obtain value for
	 * @return property value or null if there is no such property
	 */
	public Object getProperty(String property) {
		if(vertex != null && vertex.property(property).isPresent()) {
			return vertex.property(property).value();
		}
		return null;
	}
	
	/**
	 * Get single reference for provided Transponder property
	 * @param property name of a property to obtain value for
	 * @param isVertexNeeded what are we looking for: vertex or edge
	 * @return either referenced {@link Vertex} or {@link Edge} or null
	 */
	public Element getSingleReferenced(String property, boolean isVertexNeeded) {
		if(vertex != null) {
			if(vertex.edges(Direction.OUT, property).hasNext()) {
				Edge edge = vertex.edges(Direction.OUT, property).next();
				return isVertexNeeded ? edge.inVertex() : edge;
			}
		}
		return null;
	}
	
	/**
	 * Get list of referenced {@link Vertex}s or corresponding {@link Edge}s
	 * @param property name of a property to obtain value for
	 * @param isVertexNeeded what are we looking for: vertex or edge
	 * @return list of referenced {@link Vertex} or {@link Edge} or null
	 */
	public List<Element> getMultiReferenced(String property, boolean isVertexNeeded) {
		if(vertex != null) {
			return StreamSupport.stream(
					Spliterators.spliteratorUnknownSize(vertex.edges(Direction.OUT, property), 0), false)
						.map(e -> isVertexNeeded ? e.inVertex() : e)
						.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
	
	/**
	 * Sets value to actual JanusGraph property
	 * @param property name of a property to set value to
	 * @param value value to set
	 * @return this wrapper
	 */
	public VertexWrapper setProperty(String property, Object value) {
		if(vertex != null) {
			vertex.property(property, value);
		}
		return this;
	}
	
	/**
	 * Set value as single referenced {@link Vertex}
	 * @param property name of a property to set value to
	 * @param value value to set
	 * @param isVertexExpected are we setting value to vertex or edge
	 */
	public void setSingleReferenced(String property, Element value, boolean isVertexExpected) {
		if(vertex != null && value instanceof Vertex) {
			// Remove existing edges with this label
			vertex.edges(Direction.OUT, property).forEachRemaining(Edge::remove);
			// Create new edge
			vertex.addEdge(property, (Vertex)value);
		}
	}
	
	/**
	 * Set value as multi referenced {@link Vertex}
	 * @param property name of a property to set value to
	 * @param value value to set
	 * @param isVertexExpected are we setting value to vertex or edge
	 */
	public void setMultiReferenced(String property, Collection<Element> value, boolean isVertexExpected) {
		if(vertex != null) {
			// Get current edges
			List<Edge> currentEdges = StreamSupport.stream(
					Spliterators.spliteratorUnknownSize(vertex.edges(Direction.OUT, property), 0), false)
					.collect(Collectors.toList());
			
			// Remove edges that are not in the new value set
			List<Vertex> newVertices = value.stream()
					.filter(e -> e instanceof Vertex)
					.map(e -> (Vertex)e)
					.collect(Collectors.toList());
			
			currentEdges.stream()
					.filter(e -> !newVertices.contains(e.inVertex()))
					.forEach(Edge::remove);
			
			// Add new edges for vertices not already connected
			List<Vertex> currentVertices = currentEdges.stream()
					.map(Edge::inVertex)
					.collect(Collectors.toList());
			
			newVertices.stream()
					.filter(v -> !currentVertices.contains(v))
					.forEach(v -> vertex.addEdge(property, v));
		}
	}
}