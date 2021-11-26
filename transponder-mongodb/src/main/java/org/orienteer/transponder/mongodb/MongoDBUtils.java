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
	
	
	public boolean hasCollection(MongoDatabase db, String collectionName) {
		return StreamSupport.stream(db.listCollectionNames().spliterator(), false)
					.filter(n -> Objects.equals(collectionName, n)).count()>0;
	}
	
	public Object execute(MongoDatabase db, String commandStr, Map<String, Object> params, String defaultCollection) {
		Document command = Document.parse(CommonUtils.interpolate(commandStr, params));
		if(command.get("$operation")==null) {
			command = new Document("$operation", "find")
							.append("$filter", command);
		}
		return execute(db, command, defaultCollection);
	}
	
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
