package org.orienteer.transponder.neo4j;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.values.storable.Values;
import org.orienteer.transponder.CommonUtils;
import org.orienteer.transponder.Transponder;

/**
 * Base class for all Neo4J generated wrapper classes.
 * It covers {@link Entity}, so both Nodes and Relationships
 */
public class EntityWrapper {
	protected Entity entity;

	/**
	 * Creates empty {@link EntityWrapper} with no associated Neo4J entity(node/relationship)
	 */
	public EntityWrapper() {
	}

	/**
	 * Creates {@link EntityWrapper} and associate it with corresponding {@link Entity}(node/relationship)
	 * @param entity Neo4J entity to bind to
	 */
	public EntityWrapper(Entity entity) {
		this.entity = entity;
	}
	
	/**
	 * @return enclosed entity
	 */
	public Entity getEntity() {
		return entity;
	}
	
	/**
	 * Replace enclosed entity with new one
	 * @param entity new entity
	 */
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	
	/**
	 * Get an value for a {@link Transponder} property 
	 * which might be mapped to relationships, nodes or actual neo4j properties
	 * @param property name of a property to obtain value for
	 * @param type expected return type. Used to detect proper way to access value
	 * @return value of an property
	 */
	public Object get(String property, Type type) {
		Class<?> requiredClass = CommonUtils.typeToRequiredClass(type);
		if(Neo4JUtils.isSupportedPropertyClass(requiredClass)) return getProperty(property);
		else {
			Class<? extends Entity> expectedEntityClass = Neo4JUtils.getExpectedEntityClass(requiredClass);
			if(expectedEntityClass==null) return null;
			else {
				if(Collection.class.isAssignableFrom(CommonUtils.typeToMasterClass(type))) 
					return getMultiReferenced(property, Node.class.isAssignableFrom(expectedEntityClass));
				else return getSingleReferenced(property, Node.class.isAssignableFrom(expectedEntityClass));
			}
		}
	}
	
	/**
	 * Set value for a provided property
	 * @param property name of a property to set value to
	 * @param value actual value to set
	 * @param type type of an value
	 * @return this wrapper
	 */
	public EntityWrapper set(String property, Object value, Type type) {
		Class<?> requiredClass = CommonUtils.typeToRequiredClass(type);
		if(Neo4JUtils.isSupportedPropertyClass(requiredClass)) return setProperty(property, value);
		else {
			Class<? extends Entity> expectedEntityClass = Neo4JUtils.getExpectedEntityClass(requiredClass);
			if(expectedEntityClass!=null) {
				if(Collection.class.isAssignableFrom(CommonUtils.typeToMasterClass(type))) 
					setMultiReferenced(property, (Collection<Entity>)value, Node.class.isAssignableFrom(expectedEntityClass));
				else setSingleReferenced(property, (Entity)value, Node.class.isAssignableFrom(expectedEntityClass));
			}
		}
		return this;
	}
	
	/**
	 * Get Neo4J property from an entity (Node/Relationship)
	 * @param property  name of a property to obtain value for
	 * @return property value or null if there is no such property
	 */
	public Object getProperty(String property) {
		if(entity.hasProperty(property)) return entity.getProperty(property);
		else return null;
	}
	
	/**
	 * Get single reference for provided Transponder property
	 * @param property name of a property to obtain value for
	 * @param isNodeNeeded what are we looking for: node or relationship
	 * @return either referenced {@link Node} or {@link Relationship} or null
	 */
	public Entity getSingleReferenced(String property, boolean isNodeNeeded) {
		if(entity instanceof Node) {
			Node node = (Node)entity;
			Relationship relationship = node.getSingleRelationship(RelationshipType.withName(property), Direction.OUTGOING);
			return isNodeNeeded?relationship.getEndNode():relationship;
		} else if (entity instanceof Relationship) {
			Relationship relationship = (Relationship) entity;
			if("start".equals(property)) return relationship.getStartNode();
			else if("end".equals(property)) return relationship.getEndNode();
		}
		return null;
	}
	
	/**
	 * Get list of referenced {@link Node}s or corresponding {@link Relationship}s
	 * @param property name of a property to obtain value for
	 * @param isNodeNeeded what are we looking for: node or relationship
	 * @return list of referenced {@link Node} or {@link Relationship} or null
	 */
	public List<Entity> getMultiReferenced(String property, boolean isNodeNeeded) {
		if(entity instanceof Node) {
			Node node = (Node)entity;
			RelationshipType relationshipType = RelationshipType.withName(property);
			return StreamSupport.stream(node.getRelationships(Direction.OUTGOING, relationshipType)
									.spliterator(), false)
						.map(r->isNodeNeeded?r.getEndNode():r)
						.collect(Collectors.toList());
		}
		return null;
	}
	
	/**
	 * Sets value to actual Neo4J property
	 * @param property name of a property to set value to
	 * @param value value to set
	 * @return this wrapper
	 */
	public EntityWrapper setProperty(String property, Object value) {
		entity.setProperty(property, value);
		return this;
	}
	
	/**
	 * Set value as single referenced {@link Node}
	 * @param property name of a property to set value to
	 * @param value value to set
	 * @param isNodeExpected are we setting value to node or relationship
	 */
	public void setSingleReferenced(String property, Entity value, boolean isNodeExpected) {
		if(entity instanceof Node) {
			Node node = (Node)entity;
			RelationshipType relationshipType = RelationshipType.withName(property);
			Relationship relationship = node.getSingleRelationship(relationshipType, Direction.OUTGOING);
			if(relationship!=null) {
				if(Objects.equals(relationship.getEndNode(), value)) return;
				else relationship.delete();
			}
			if(value instanceof Node) 
				node.createRelationshipTo((Node)value, relationshipType);
		}
	}
	
	/**
	 * Set value as multi referenced {@link Node}
	 * @param property name of a property to set value to
	 * @param value value to set
	 * @param isNodeExpected are we setting value to node or relationship
	 */
	public void setMultiReferenced(String property, Collection<Entity> value, boolean isNodeExpected) {
		if(entity instanceof Node) {
			Node node = (Node)entity;
			RelationshipType relationshipType = RelationshipType.withName(property);
			node.getRelationships(Direction.OUTGOING, relationshipType).forEach(r->{
				if(!value.contains(r.getEndNode())) r.delete();
			});
			List<Entity> remaining = getMultiReferenced(property, true);
			List<Entity> toAdd = new ArrayList<>(value);
			toAdd.removeAll(remaining);
			toAdd.forEach(n -> {
				node.createRelationshipTo((Node)n, relationshipType);
			});
		}
	}
	
}
