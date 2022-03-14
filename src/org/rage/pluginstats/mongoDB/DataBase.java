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
	private MongoCollection<Document> collection, discordCollection;		
	
	private static FileConfiguration config;
	private final String CONNECTION = "mongoURL", 
						 DATABASE_NAME = "dataBaseName",
						 COLLECTION_NAME = "collectionName",
						 DISCORD_DATABASE_NAME = "discordCollectionName";
	
	public DataBase(FileConfiguration config){
		DataBase.config = config; 
		this.client = new MongoClient(new MongoClientURI(config.getString(CONNECTION)));
		database = client.getDatabase(config.getString(DATABASE_NAME));
		discordCollection = database.getCollection(config.getString(DISCORD_DATABASE_NAME));
		collection = database.getCollection(config.getString(COLLECTION_NAME));
	}
	
	public static FileConfiguration getConfig() {
		return config;
	}
	
	public MongoCollection<Document> getServerCollection() {
		return collection;
	}
	
	public MongoCollection<Document> getDiscordCollection() {
	 	return discordCollection;
	}
	
	public MongoDatabase getDatabase() {
		return database;
	}
	
	public Document getDiscordUser(String userId) {
		return discordCollection.find(new Document("userId", userId)).first();
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
