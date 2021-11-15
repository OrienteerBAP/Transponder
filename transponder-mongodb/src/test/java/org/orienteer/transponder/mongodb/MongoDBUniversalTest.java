package org.orienteer.transponder.mongodb;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.orienteer.transponder.AbstractUniversalTest;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import de.flapdoodle.embed.process.runtime.Network;

public class MongoDBUniversalTest extends AbstractUniversalTest {
	
	private static MongodExecutable mongodExecutable;
	private static MongoClient mongo;
	private static MongodForTestsFactory mongoFactory;
	private static MongoDatabase mongoDb;
	
	@BeforeClass
	public static void initMongoDB() throws Exception {
		MongodStarter starter = MongodStarter.getDefaultInstance();

		int port = Network.freeServerPort(Network.getLocalHost());
		MongodConfig mongodConfig = MongodConfig.builder()
		    .version(Version.Main.PRODUCTION)
		    .net(new Net(port, Network.localhostIsIPv6()))
		    .build();

		  mongodExecutable = starter.prepare(mongodConfig);
		  MongodProcess mongod = mongodExecutable.start();
		  mongo = new MongoClient("localhost", port);
		  mongoFactory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
		  mongoDb = mongoFactory.newDatabase(mongo);
	}
	
	@AfterClass
	public static void shutdownMongoDB() throws Exception {
		if(mongoFactory!=null) mongoFactory.shutdown();
		if(mongodExecutable!=null) mongodExecutable.stop();
	}
	
	public MongoDBUniversalTest() {
		super(new MongoDBTestDriver(mongoDb));
	}
}
