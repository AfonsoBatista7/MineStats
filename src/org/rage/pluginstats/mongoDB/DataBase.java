package org.rage.pluginstats.mongoDB;

import java.util.UUID;

import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;
import org.rage.pluginstats.stats.Stats;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public class DataBase {
	
	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<Document> collection;		
	
	private final String CONNECTION = "mongoURL";
	private final String DATABASE_NAME = "dataBaseName";
	private final String COLLECTION_NAME = "collectionName";
	private final FileConfiguration config;
	
	
	
	public DataBase(FileConfiguration config){
		this.config = config; 
		this.client = new MongoClient(new MongoClientURI(config.getString(CONNECTION)));
		database = client.getDatabase(config.getString(DATABASE_NAME));
		collection = database.getCollection(config.getString(COLLECTION_NAME));
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public MongoCollection<Document> getCollection() {
		return collection;
	}
	
	public MongoDatabase getDatabase() {
		return database;
	}
	
	public Document getPlayer(UUID playerId) {
		return collection.find(new Document(Stats.PLAYERID.getQuery(), playerId)).first();
	}
	
	public Document getPlayerByName(String playerName) {
		return collection.find(new Document(Stats.NAME.getQuery(), playerName)).first();
	}
	
	public void newDoc(Document playerDoc) {
		collection.insertOne(playerDoc);
	}
	
}
