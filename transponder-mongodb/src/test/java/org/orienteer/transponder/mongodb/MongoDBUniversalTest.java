package org.orienteer.transponder.mongodb;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.orienteer.transponder.AbstractUniversalTest;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.MongodStarter;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker.ReachedState;

public class MongoDBUniversalTest extends AbstractUniversalTest {
	
	private static ReachedState<RunningMongodProcess> running;
	private static MongoClient mongo;
	private static MongoDatabase mongoDb;
	
	@BeforeClass
	public static void initMongoDB() throws Exception {
		running = Mongod.instance().start(Version.Main.V5_0);
		
		mongo = MongoClients.create("mongodb://"+running.current().getServerAddress());
		
		mongoDb = mongo.getDatabase("test-db");
	}
	
	@AfterClass
	public static void shutdownMongoDB() throws Exception {
		if(mongoDb!=null) mongoDb.drop();
		if(mongo!=null) mongo.close();
		if(running!=null) running.close();
	}
	
	public MongoDBUniversalTest() {
		super(new MongoDBTestDriver(mongoDb));
	}
}
