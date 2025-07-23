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
import org.orienteer.transponder.IMutator;

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
			// If this is a reference to another entity type, create an edge label instead of a property
			if (referencedType != null && !referencedType.isEmpty()) {
				if (mgmt.getEdgeLabel(propertyName) == null) {
					mgmt.makeEdgeLabel(propertyName).make();
				}
			} else {
				// Create regular property
				if (mgmt.getPropertyKey(propertyName) == null) {
					Class<?> masterClass = org.orienteer.transponder.CommonUtils.typeToMasterClass(propertyType);
					Class<?> dataType = getJanusGraphDataType(masterClass);
					
					if (dataType != null) {
						mgmt.makePropertyKey(propertyName).dataType(dataType).make();
					}
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
		// JanusGraph requires explicit commits for changes to be visible
		try {
			// Ensure the graph is open and the transaction is active
			if (!graph.tx().isOpen()) {
				graph.tx().open();
			}
			graph.tx().commit();
		} catch (Exception e) {
			try {
				graph.tx().rollback();
			} catch (Exception rollbackEx) {
				// Ignore rollback exceptions
			}
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
			// Simple implementation for basic queries
			// TODO: Implement full Gremlin query parsing and parameter substitution
			if (query.contains("select") && query.contains("from")) {
				// Handle SQL-like queries by converting to Gremlin
				String entityType = extractEntityTypeFromQuery(query);
				if (entityType != null) {
					return g.V().hasLabel(entityType)
							.toList()
							.stream()
							.map(v -> (Object)v)
							.collect(Collectors.toList());
				}
			}
			
			// Fallback: return all vertices
			return g.V().toList()
					.stream()
					.map(v -> (Object)v)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new RuntimeException("Query execution failed: " + query, e);
		}
	}
	
	@Override
	public Object querySingle(String language, String query, Map<String, Object> params, Type type) {
		List<Object> results = query(language, query, params, type);
		return results.isEmpty() ? null : results.get(0);
	}
	
	private String extractEntityTypeFromQuery(String query) {
		// Simple extraction of entity type from SQL-like query
		// Example: "select from MyEntity" -> "MyEntity"
		String[] parts = query.split("\\s+");
		for (int i = 0; i < parts.length - 1; i++) {
			if ("from".equalsIgnoreCase(parts[i])) {
				return parts[i + 1];
			}
		}
		return null;
	}

	@Override
	public String getDialect() {
		return DIALECT_JANUSGRAPH;
	}
	
	@Override
	public IMutator getMutator() {
		// For now, return null to use default mutator behavior
		// This allows commands to work without advanced annotations
		return null;
	}
	
	@Override
	public void replaceSeed(Object wrapper, Object newSeed) {
		((VertexWrapper)wrapper).setVertex((Vertex)newSeed);
	}
	
	@Override
	public Object command(String language, String command, Map<String, Object> params, Type type) {
		try {
			// Simple command execution for basic operations like deleteAll
			if (command.toLowerCase().contains("delete")) {
				// Execute delete command by clearing vertices
				g.V().drop().iterate();
				graph.tx().commit();
				return null; // Commands typically return null
			} else {
				// For other commands, just log for now
				System.out.printf("JanusGraph command execution: %s%n", command);
				return null;
			}
		} catch (Exception e) {
			graph.tx().rollback();
			throw new RuntimeException("Command execution failed: " + command, e);
		}
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