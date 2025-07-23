package org.orienteer.transponder.janusgraph;

import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.orienteer.transponder.ITestDriver;

public class JanusGraphTestDriver extends JanusGraphDriver implements ITestDriver {
	
	public JanusGraphTestDriver(JanusGraph graph) {
		super(graph);
	}

	@Override
	public boolean hasType(String typeName) {
		JanusGraphManagement mgmt = getGraph().openManagement();
		try {
			boolean exists = mgmt.getVertexLabel(typeName) != null;
			mgmt.rollback();
			return exists;
		} catch (Exception e) {
			mgmt.rollback();
			return false;
		}
	}

	@Override
	public boolean hasProperty(String typeName, String propertyName) {
		JanusGraphManagement mgmt = getGraph().openManagement();
		try {
			boolean exists = mgmt.getPropertyKey(propertyName) != null;
			mgmt.rollback();
			return exists;
		} catch (Exception e) {
			mgmt.rollback();
			return false;
		}
	}

	@Override
	public boolean hasReferenceProperty(String typeName, String propertyName, String referenceType) {
		JanusGraphManagement mgmt = getGraph().openManagement();
		try {
			boolean exists = mgmt.getEdgeLabel(propertyName) != null;
			mgmt.rollback();
			return exists;
		} catch (Exception e) {
			mgmt.rollback();
			return false;
		}
	}

	@Override
	public boolean hasIndex(String typeName, String indexName, String... properties) {
		JanusGraphManagement mgmt = getGraph().openManagement();
		try {
			boolean exists = mgmt.getGraphIndex(indexName) != null;
			mgmt.rollback();
			return exists;
		} catch (Exception e) {
			mgmt.rollback();
			return false;
		}
	}

	@Override
	public Object createSeedObject(String typeName, Map<String, ?> properties) {
		try {
			Vertex vertex = getGraph().addVertex(T.label, typeName);
			for (Map.Entry<String, ?> entry : properties.entrySet()) {
				vertex.property(entry.getKey(), entry.getValue());
			}
			getGraph().tx().commit();
			return vertex;
		} catch (Exception e) {
			getGraph().tx().rollback();
			throw new RuntimeException("Failed to create seed object", e);
		}
	}
}