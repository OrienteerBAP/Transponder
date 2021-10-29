package org.orienteer.transponder.neo4j;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.spatial.Point;
import org.orienteer.transponder.annotation.EntityType;

import lombok.experimental.UtilityClass;

/**
 * Neo4J specific utility methods
 */
@UtilityClass
public class Neo4JUtils {
	
	private static final Class<?>[] SUPPORTED_VALUES_CLASSES = 
			{String.class, Boolean.class, Number.class, Character.class, Temporal.class,
			 TemporalAmount.class, Point.class};
	
	/**
	 * Try to extract type from provided {@link Node} or {@link Relationship}
	 * @param entity to lookup transponder type from
	 * @return set of types candidates
	 */
	public List<String> entityToType(Entity entity) {
		List<String> ret = new ArrayList<>();
		if(entity instanceof Node) {
			((Node)entity).getLabels().forEach(l -> ret.add(l.name()));
		} else if(entity instanceof Relationship) {
			ret.add(((Relationship)entity).getType().name());
		}
		return ret;
	}
	
	/**
	 * Check whether provided class of value can be stored as property of {@link Node} or {@link Relationship}
	 * @param valueClass class of a value to check
	 * @return true if value can be stored as property
	 */
	public boolean isSupportedPropertyClass(Class<?> valueClass) {
		if(valueClass.isArray()) valueClass = valueClass.getComponentType();
		for (Class<?> supportedClass : SUPPORTED_VALUES_CLASSES) {
			if(supportedClass.isAssignableFrom(valueClass)) return true;
		}
		return false;
	}
	
	/**
	 * Check whether provided entity represents {@link Node} or {@link Relationship}
	 * @param entity class to check
	 * @return corresponding Class ( {@link Node} or {@link Relationship} ) or null
	 */
	public Class<? extends Entity> getExpectedEntityClass(Class<?> entity) {
		return entity.getAnnotation(EntityType.class)!=null
				?(entity.getAnnotation(Neo4JRelationship.class)!=null?Relationship.class:Node.class)
				:null;
	}
}
