package org.orienteer.transponder.mongodb;

import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.orienteer.transponder.CommonUtils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import lombok.experimental.UtilityClass;

/**
 * MongoDB specific utility methods
 */
@UtilityClass
public class MongoDBUtils {
	
	/**
	 * Check for presence of specific collection in the db
	 * @param db mongo db to check presence of collection in
	 * @param collectionName name of a collection to check for
	 * @return true if there is such collection
	 */
	public boolean hasCollection(MongoDatabase db, String collectionName) {
		return StreamSupport.stream(db.listCollectionNames().spliterator(), false)
					.filter(n -> Objects.equals(collectionName, n)).count()>0;
	}
	
	/**
	 * Execute specified command in MongoDB. Command has the following structure:
	 * <pre>
	 * {$operation : "select" | "find" | "update" | "delete" | "count", 
	 * $filter : ...,
	 * $update : ... (for update) 
	 * }
	 * </pre>
	 * @param db instance of MongoDB to execute command in
	 * @param commandStr string representation of a command. 
	 * @param params - parameters for interpolation
	 * @param defaultCollection default collection to execute in
	 * @return result of command execution
	 */
	public Object execute(MongoDatabase db, String commandStr, Map<String, Object> params, String defaultCollection) {
		Document command = Document.parse(CommonUtils.interpolate(commandStr, params));
		if(command.get("$operation")==null) {
			command = new Document("$operation", "find")
							.append("$filter", command);
		}
		return execute(db, command, defaultCollection);
	}
	
	/**
	 * Execute specified command in MongoDB. Command has the following structure:
	 * <pre>
	 * {$operation : "select" | "find" | "update" | "delete" | "count", 
	 * $filter : ...,
	 * $update : ... (for update) 
	 * }
	 * </pre>
	 * @param db instance of MongoDB to execute command in
	 * @param command command to execute
	 * @param defaultCollection default collection to execute in
	 * @return result of command execution
	 */
	public Object execute(MongoDatabase db, Document command, String defaultCollection) {
		String collectionName = (String)CommonUtils.defaultIfNull(command.get("$collection"), defaultCollection);
		MongoCollection<Document> collection = db.getCollection(collectionName);
		String operation = command.getString("$operation");
		switch (operation) {
			case "delete":
				return collection.deleteMany((Bson)command.get("$filter", new Document()));
			case "update":
				return collection.updateMany((Bson)command.get("$filter", new Document()),
											 (Bson)command.get("$update"));
			case "count":
				return collection.countDocuments((Bson)command.get("$filter", new Document()));
			case "select":
			case "find":
			default:
				return collection.find((Bson)command.get("$filter", new Document()));
		}
	}
	
}
