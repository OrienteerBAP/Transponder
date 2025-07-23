package org.orienteer.transponder.janusgraph;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.VertexLabel;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.orienteer.transponder.IDriver;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Transponder {@link IDriver} for JanusGraph
 */
public class JanusGraphDriver implements IDriver {
	
	public static final String DIALECT_JANUSGRAPH = "janusgraph";
	
	public static final String TYPE_CUSTOM_TRANSPONDER_WRAPPER = "transponder.wrapper";
	
	private static final BiMap<String, Class<?>> TYPE_TO_MAIN_CLASS = HashBiMap.create(); 
	
	private final JanusGraph graph;
	private final GraphTraversalSource g;
	
	/**
	 * Creates {@link IDriver} which associated with provided JanusGraph instance
	 * @param graph JanusGraph instance to associate driver with
	 */
	public JanusGraphDriver(JanusGraph graph) {
		this.graph = graph;
		this.g = graph.traversal();
	}
	
	@Override
	public void createType(String typeName, boolean isAbstract, Class<?> mainWrapperClass, String... superTypes) {
		JanusGraphManagement mgmt = graph.openManagement();
		try {
			VertexLabel label = mgmt.getVertexLabel(typeName);
			if (label == null) {
				if (isAbstract) {
					// JanusGraph doesn't have explicit abstract concept, but we can track it
					label = mgmt.makeVertexLabel(typeName).setStatic().make();
				} else {
					label = mgmt.makeVertexLabel(typeName).make();
				}
			}
			mgmt.commit();
		} catch (Exception e) {
			mgmt.rollback();
			throw new RuntimeException("Failed to create type: " + typeName, e);
		}
		
		TYPE_TO_MAIN_CLASS.put(typeName, mainWrapperClass);
	}

	@Override
	public void createProperty(String typeName, String propertyName, Type propertyType, String referencedType,
			int order, AnnotatedElement annotations) {
		JanusGraphManagement mgmt = graph.openManagement();
		try {
			if (mgmt.getPropertyKey(propertyName) == null) {
				Class<?> masterClass = org.orienteer.transponder.CommonUtils.typeToMasterClass(propertyType);
				Class<?> dataType = getJanusGraphDataType(masterClass);
				
				if (dataType != null) {
					mgmt.makePropertyKey(propertyName).dataType(dataType).make();
				}
			}
			mgmt.commit();
		} catch (Exception e) {
			mgmt.rollback();
			throw new RuntimeException("Failed to create property: " + propertyName, e);
		}
	}

	@Override
	public void setupRelationship(String type1Name, String property1Name, String type2Name, String property2Name) {
		JanusGraphManagement mgmt = graph.openManagement();
		try {
			if (mgmt.getEdgeLabel(property1Name) == null) {
				mgmt.makeEdgeLabel(property1Name).make();
			}
			mgmt.commit();
		} catch (Exception e) {
			mgmt.rollback();
			throw new RuntimeException("Failed to setup relationship: " + property1Name, e);
		}
	}

	@Override
	public void createIndex(String typeName, String indexName, String indexType, AnnotatedElement annotations,
			String... properties) {
		JanusGraphManagement mgmt = graph.openManagement();
		try {
			// Create composite index for the properties
			if (mgmt.getGraphIndex(indexName) == null && properties.length > 0) {
				mgmt.buildIndex(indexName, Vertex.class)
					.addKey(mgmt.getPropertyKey(properties[0]))
					.buildCompositeIndex();
			}
			mgmt.commit();
		} catch (Exception e) {
			mgmt.rollback();
			// Index creation might fail if already exists, which is okay
		}
	}

	@Override
	public Object getPropertyValue(Object wrapper, String property, Type type) {
		return ((VertexWrapper)wrapper).get(property, type);
	}

	@Override
	public void setPropertyValue(Object wrapper, String property, Object value, Type type) {
		((VertexWrapper)wrapper).set(property, value, type);
	}

	@Override
	public <T> T newEntityInstance(Class<T> proxyClass, String type) {
		try {
			Vertex vertex = graph.addVertex(org.apache.tinkerpop.gremlin.structure.T.label, type);
			return proxyClass.getConstructor(Vertex.class).newInstance(vertex);
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't create new entityInstance for class " + proxyClass + " with VertexLabel " + type, e);
		}
	}

	@Override
	public void saveEntityInstance(Object wrapper) {
		// JanusGraph commits changes automatically within transactions
		// We could add explicit transaction handling here if needed
		try {
			graph.tx().commit();
		} catch (Exception e) {
			graph.tx().rollback();
			throw new RuntimeException("Failed to save entity", e);
		}
	}

	@Override
	public <T> T wrapEntityInstance(Class<T> proxyClass, Object seed) {
		try {
			return proxyClass.getConstructor(Vertex.class).newInstance((Vertex)seed);
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't wrap seed by class " + proxyClass + ". Seed: " + seed, e);
		}
	}

	@Override
	public Class<?> getDefaultEntityBaseClass() {
		return VertexWrapper.class;
	}

	@Override
	public Class<?> getEntityMainClass(Object seed) {
		if (seed instanceof Vertex) {
			String label = ((Vertex)seed).label();
			return TYPE_TO_MAIN_CLASS.get(label);
		}
		return null;
	}

	@Override
	public boolean isSeedClass(Class<?> seedClass) {
		return Element.class.isAssignableFrom(seedClass);
	}

	@Override
	public Object toSeed(Object wrapped) {
		return ((VertexWrapper)wrapped).getVertex();
	}

	@Override
	public List<Object> query(String language, String query, Map<String, Object> params, Type type) {
		try {
			// For now, assume Gremlin queries
			return g.V().has("name", params.get("name"))
					.toList()
					.stream()
					.map(v -> (Object)v)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new RuntimeException("Query execution failed: " + query, e);
		}
	}

	@Override
	public String getDialect() {
		return DIALECT_JANUSGRAPH;
	}
	
	@Override
	public void replaceSeed(Object wrapper, Object newSeed) {
		((VertexWrapper)wrapper).setVertex((Vertex)newSeed);
	}

	/**
	 * @return associated JanusGraph instance
	 */
	public JanusGraph getGraph() {
		return graph;
	}
	
	/**
	 * @return GraphTraversalSource for this graph
	 */
	public GraphTraversalSource traversal() {
		return g;
	}
	
	/**
	 * Map Java class to JanusGraph data type
	 * @param javaClass Java class to map
	 * @return JanusGraph compatible data type class
	 */
	private Class<?> getJanusGraphDataType(Class<?> javaClass) {
		if (String.class.equals(javaClass)) return String.class;
		if (Integer.class.equals(javaClass) || int.class.equals(javaClass)) return Integer.class;
		if (Long.class.equals(javaClass) || long.class.equals(javaClass)) return Long.class;
		if (Double.class.equals(javaClass) || double.class.equals(javaClass)) return Double.class;
		if (Float.class.equals(javaClass) || float.class.equals(javaClass)) return Float.class;
		if (Boolean.class.equals(javaClass) || boolean.class.equals(javaClass)) return Boolean.class;
		return null;
	}
}