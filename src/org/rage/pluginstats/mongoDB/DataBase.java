package org.rage.pluginstats.mongoDB;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.bukkit.configuration.file.FileConfiguration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;

/**
 * Manages MongoDB connection and raw collection operations.
 * Collections: users, identities, gamestats
 * @author Afonso Batista
 * 2021 - 2023
 */
public class DataBase {

    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> usersCollection;
    private MongoCollection<Document> identitiesCollection;
    private MongoCollection<Document> gamestatsCollection;

    private static FileConfiguration config;

    private static final String CONNECTION             = "mongoURL";
    private static final String DATABASE_NAME         = "dataBaseName";
    private static final String USERS_COLLECTION      = "usersCollectionName";
    private static final String IDENTITIES_COLLECTION = "identitiesCollectionName";
    private static final String GAMESTATS_COLLECTION  = "gamestatsCollectionName";
    private static final String SERVER_ID_KEY         = "serverId";

    public DataBase(FileConfiguration config) {
        DataBase.config = config;
        this.client   = new MongoClient(new MongoClientURI(config.getString(CONNECTION)));
        this.database = client.getDatabase(config.getString(DATABASE_NAME));
        this.usersCollection      = database.getCollection(config.getString(USERS_COLLECTION,      "users"));
        this.identitiesCollection = database.getCollection(config.getString(IDENTITIES_COLLECTION, "identities"));
        this.gamestatsCollection  = database.getCollection(config.getString(GAMESTATS_COLLECTION,  "gamestats"));
    }

    public static FileConfiguration getConfig() { return config; }

    public String getServerId() { return config.getString(SERVER_ID_KEY, ""); }

    // -------------------------------------------------------------------------
    // Identity operations
    // -------------------------------------------------------------------------

    public Document getIdentityByExternalId(String externalId, String provider) {
        return identitiesCollection.find(
            new Document(DBFields.EXTERNAL_ID, externalId)
                .append(DBFields.PROVIDER, provider)
        ).first();
    }

    public Document getIdentityByUsername(String username, String provider) {
        return identitiesCollection.find(
            new Document(DBFields.USERNAME, username)
                .append(DBFields.PROVIDER, provider)
        ).first();
    }

    public Document getIdentityByUserId(ObjectId userId, String provider) {
        return identitiesCollection.find(
            new Document(DBFields.USER_ID, userId)
                .append(DBFields.PROVIDER, provider)
        ).first();
    }

    public Document getIdentityById(String id) {
        return identitiesCollection.find(new Document("_id", new ObjectId(id))).first();
    }

    public void insertIdentity(Document doc) { identitiesCollection.insertOne(doc); }

    public void updateIdentity(Bson filter, Bson update) {
        identitiesCollection.updateOne(filter, update);
    }

    // -------------------------------------------------------------------------
    // User (physical person) operations
    // -------------------------------------------------------------------------

    public void insertUser(Document doc) { usersCollection.insertOne(doc); }

    // -------------------------------------------------------------------------
    // Gamestat operations
    // -------------------------------------------------------------------------

    public Document getGamestatByIdentityId(String identityId, String serverId) {
        return gamestatsCollection.find(
            new Document(DBFields.IDENTITY_ID, identityId)
                .append(DBFields.SERVER_ID, serverId)
        ).first();
    }

    public void insertGamestat(Document doc) { gamestatsCollection.insertOne(doc); }

    public void updateGamestat(Bson filter, Bson update) {
        gamestatsCollection.updateOne(filter, update);
    }

    public void updateManyGamestat(Bson filter, Bson update) {
        gamestatsCollection.updateMany(filter, update);
    }

    public void deleteGamestat(Bson filter) { gamestatsCollection.deleteOne(filter); }

    public MongoCollection<Document> getGamestatsCollection() { return gamestatsCollection; }
}
