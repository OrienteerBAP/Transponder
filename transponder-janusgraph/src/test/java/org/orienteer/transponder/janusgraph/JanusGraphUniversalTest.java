package org.orienteer.transponder.janusgraph;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.orienteer.transponder.AbstractUniversalTest;

public class JanusGraphUniversalTest extends AbstractUniversalTest {
	private static JanusGraph graph;
	
	public JanusGraphUniversalTest() {
		super(new JanusGraphTestDriver(getGraph()));
	}
	
	@BeforeAll
	public static void initDb() {
		getGraph(); // Initialize database
	}
	
	@AfterAll
	public static void closeDb() {
		if (graph != null && !graph.isClosed()) {
			try {
				graph.close();
			} catch (Exception e) {
				// Ignore close exceptions
			}
		}
	}
	
	public static JanusGraph getGraph() {
		if (graph == null || graph.isClosed()) {
			try {
				// Use simple in-memory configuration for testing without external indexing
				graph = JanusGraphFactory.build()
					.set("storage.backend", "inmemory")
					.open();
			} catch (Exception e) {
				throw new RuntimeException("Failed to initialize JanusGraph database", e);
			}
		}
		return graph;
	}
}