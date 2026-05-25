package org.rage.pluginstats.mongoDB;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.Block;
import org.rage.pluginstats.stats.BlockStats;
import org.rage.pluginstats.stats.GamestatField;
import org.rage.pluginstats.stats.Mob;
import org.rage.pluginstats.stats.MobStats;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.Util;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

/**
 * @author Afonso Batista
 * 2021 - 2022
 */
public class DataBaseManager {

    private Logger log;
    private DataBase mongoDB;
    private ServerManager serverManager;

    public DataBaseManager(DataBase mongoDB, Logger log, ServerManager serverManager) {
        this.log = log;
        this.mongoDB = mongoDB;
        this.serverManager = serverManager;
    }

    // -------------------------------------------------------------------------
    // Player lifecycle
    // -------------------------------------------------------------------------

    /**
     * Creates a new identity (if needed) + gamestat for this player on this server.
     * Returns an enriched gamestat document.
     */
    public Document newPlayer(Player player) {
        String uuidStr  = player.getUniqueId().toString();
        String serverId = mongoDB.getServerId();

        Document identityDoc = mongoDB.getIdentityByExternalId(uuidStr, "minecraft");
        if (identityDoc == null) {
            identityDoc = new Document(DBFields.EXTERNAL_ID, uuidStr)
                .append(DBFields.USERNAME, player.getName())
                .append(DBFields.PROVIDER, "minecraft");
            mongoDB.insertIdentity(identityDoc);
        } else if (!player.getName().equals(identityDoc.getString(DBFields.USERNAME))) {
            mongoDB.updateIdentity(
                Filters.eq("_id", identityDoc.getObjectId("_id")),
                Updates.set(DBFields.USERNAME, player.getName())
            );
            identityDoc.put(DBFields.USERNAME, player.getName());
        }

        String identityId = identityDoc.getObjectId("_id").toString();

        Document statsSubDoc = new Document();
        for (Stats stat : Stats.values())
            statsSubDoc.append(stat.getQuery(), stat.getFirstValue());

        Document gamestatDoc = new Document(DBFields.IDENTITY_ID, identityId)
            .append(DBFields.SERVER_ID, serverId);
        for (GamestatField field : GamestatField.values())
            if (field.getFirstValue() != null)
                gamestatDoc.append(field.getQuery(), field.getFirstValue());
        gamestatDoc.append(DBFields.STATS, statsSubDoc);

        mongoDB.insertGamestat(gamestatDoc);

        Bukkit.broadcastMessage(Util.chat(
            "&b[MineStats]&7 - Heyyy &a<player>&7! Welcome to Minecraft Nostalgia :D."
                .replace("<player>", player.getName())));

        return enrichGamestatWithIdentity(gamestatDoc, identityDoc);
    }

    public ServerPlayer getPlayerStats(Player player) {
        if (serverManager.playerAlreadyInServer(player.getUniqueId()))
            return serverManager.getPlayerStats(player.getUniqueId());

        ServerPlayer pp = new ServerPlayer(player.getUniqueId(), this);
        Document playerDoc = getPlayer(player.getUniqueId());

        if (playerDoc == null) {
            // UUID may have changed — try by name
            Document identityDoc = mongoDB.getIdentityByUsername(player.getName(), "minecraft");
            if (identityDoc != null) {
                serverManager.deleteFromHashMap(UUID.fromString(identityDoc.getString(DBFields.EXTERNAL_ID)));
                mongoDB.updateIdentity(
                    Filters.eq("_id", identityDoc.getObjectId("_id")),
                    Updates.set(DBFields.EXTERNAL_ID, player.getUniqueId().toString())
                );
            }
            playerDoc = newPlayer(player);
        }

        try {
            downloadFromDataBase(pp, playerDoc);
        } catch (ParseException e) {
            log.log(Level.SEVERE, "[MineStats] - An error has occurred:", e);
        }

        return pp;
    }

    // -------------------------------------------------------------------------
    // Download / Upload
    // -------------------------------------------------------------------------

    public void downloadFromDataBase(ServerPlayer sp, Document playerDoc) throws ParseException {
        synchronized (serverManager) {
            sp.setName(playerDoc.getString(DBFields.NAME));
            sp.setIdentityId(playerDoc.getString(DBFields.IDENTITY_ID));

            Document statsDoc = playerDoc.get(DBFields.STATS, Document.class);
            if (statsDoc == null) statsDoc = new Document();

            sp.setBlockStats(new BlockStats(
                getLongOrZero(statsDoc, Stats.BLOCKSDEST.getQuery()),
                getLongOrZero(statsDoc, Stats.BLOCKSPLA.getQuery()),
                getLongOrZero(statsDoc, Stats.REDSTONEUSED.getQuery()),
                getLongOrZero(statsDoc, Stats.BLOCKSMINED.getQuery()),
                loadBlockStats(statsDoc.getList(Stats.BLOCKS.getQuery(), Document.class))
            ));
            sp.setMobStats(new MobStats(
                getLongOrZero(statsDoc, Stats.KILLS.getQuery()),
                getLongOrZero(statsDoc, Stats.MOBKILLS.getQuery()),
                getLongOrZero(statsDoc, Stats.ENDERDRAGONKILLS.getQuery()),
                getLongOrZero(statsDoc, Stats.WITHERKILLS.getQuery()),
                getLongOrZero(statsDoc, Stats.FISHCAUGHT.getQuery()),
                loadMobStats(statsDoc.getList(Stats.MOBSKILLED.getQuery(), Document.class))
            ));
            sp.setMetersTraveled(getLongOrZero(statsDoc, Stats.TRAVELLED.getQuery()));
            sp.setDeaths(getLongOrZero(statsDoc, Stats.DEATHS.getQuery()));
            sp.setTimesLogin(getLongOrZero(statsDoc, Stats.TIMESLOGIN.getQuery()));

            List<String> versions = playerDoc.getList(GamestatField.VERSIONS.getQuery(), String.class);
            sp.setNumberOfVersions(versions != null ? versions.size() : 0);
            sp.setTimePlayed(getLongOrZero(playerDoc, GamestatField.TIMEPLAYED.getQuery()));
            sp.setTimeAFK(getLongOrZero(playerDoc, GamestatField.TIMEAFK.getQuery()));
            sp.setSessionMarkTime(null);
            sp.setMedals(loadMedals(getListOrEmpty(playerDoc, GamestatField.MEDALS.getQuery())));

            List<String> tags = playerDoc.getList(GamestatField.CUSTOMTAGS.getQuery(), String.class);
            sp.setCustomTags(tags != null ? tags.toArray(new String[0]) : new String[0]);

            String defaultDate     = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            String defaultDateTime = new SimpleDateFormat("dd/MM/yyyy h:mm a").format(new Date());

            sp.setPlayerSince(new SimpleDateFormat("dd/MM/yyyy").parse(
                getStringOrDefault(playerDoc, GamestatField.PLAYERSINCE.getQuery(), defaultDate)));

            String lastLoginStr = getStringOrDefault(playerDoc, GamestatField.LASTLOGIN.getQuery(), defaultDateTime);
            try {
                sp.setLastLogin(new SimpleDateFormat("dd/MM/yyyy h:mm a").parse(lastLoginStr));
            } catch (ParseException e) {
                lastLoginStr = lastLoginStr + " 12:00 AM";
                mongoDB.updateGamestat(
                    Filters.eq(DBFields.IDENTITY_ID, sp.getIdentityId()),
                    Updates.set(GamestatField.LASTLOGIN.getQuery(), lastLoginStr)
                );
                sp.setLastLogin(new SimpleDateFormat("dd/MM/yyyy h:mm a").parse(lastLoginStr));
            }

            sp.setDisplayName(playerDoc.getString(GamestatField.DISPLAYNAME.getQuery()));
            sp.setListName(playerDoc.getString(GamestatField.LISTNAME.getQuery()));

            serverManager.newPlayerOnServer(sp);
        }
    }

    public synchronized void uploadToDataBase(ServerPlayer sp) {
        Document statsSubDoc = new Document();
        for (Stats stat : Stats.values())
            statsSubDoc.put(stat.getQuery(), Util.getStatVariable(sp, stat));

        List<Bson> updates = new ArrayList<>();
        updates.add(Updates.set(DBFields.STATS, statsSubDoc));
        for (GamestatField field : GamestatField.values()) {
            if (field.toUpload()) {
                Object value = Util.getGamestatFieldValue(sp, field);
                if (value != null) updates.add(Updates.set(field.getQuery(), value));
            }
        }

        mongoDB.updateGamestat(
            Filters.eq(DBFields.IDENTITY_ID, sp.getIdentityId()),
            Updates.combine(updates)
        );
    }

    // -------------------------------------------------------------------------
    // Player lookups — return enriched gamestat docs
    // -------------------------------------------------------------------------

    public Document getPlayer(UUID playerId) {
        Document identityDoc = mongoDB.getIdentityByExternalId(playerId.toString(), "minecraft");
        if (identityDoc == null) return null;
        Document gamestatDoc = mongoDB.getGamestatByIdentityId(
            identityDoc.getObjectId("_id").toString(), mongoDB.getServerId());
        return enrichGamestatWithIdentity(gamestatDoc, identityDoc);
    }

    public Document getPlayerByName(String name) {
        Document identityDoc = mongoDB.getIdentityByUsername(name, "minecraft");
        if (identityDoc == null) return null;
        Document gamestatDoc = mongoDB.getGamestatByIdentityId(
            identityDoc.getObjectId("_id").toString(), mongoDB.getServerId());
        return enrichGamestatWithIdentity(gamestatDoc, identityDoc);
    }

    // -------------------------------------------------------------------------
    // Discord / linking lookups
    // -------------------------------------------------------------------------

    public Document getDiscordUserByPlayer(UUID playerId) {
        Document mcIdentity = mongoDB.getIdentityByExternalId(playerId.toString(), "minecraft");
        if (mcIdentity == null || mcIdentity.get(DBFields.USER_ID) == null) return null;
        return mongoDB.getIdentityByUserId(mcIdentity.getObjectId(DBFields.USER_ID), "discord");
    }

    public Document getDiscordUser(String discordUserId) {
        return mongoDB.getIdentityByExternalId(discordUserId, "discord");
    }

    // -------------------------------------------------------------------------
    // Versions
    // -------------------------------------------------------------------------

    public boolean checkVersions(String version, Document playerDoc) {
        List<String> versions = playerDoc.getList(GamestatField.VERSIONS.getQuery(), String.class);
        if (versions != null && versions.contains(version)) return true;
        mongoDB.updateGamestat(
            Filters.eq(DBFields.IDENTITY_ID, playerDoc.getString(DBFields.IDENTITY_ID)),
            Updates.addToSet(GamestatField.VERSIONS.getQuery(), version)
        );
        return false;
    }

    // -------------------------------------------------------------------------
    // Medals
    // -------------------------------------------------------------------------

    public void levelUpMedal(Player player, Medal medal) {
        Document playerDoc = getPlayer(player.getUniqueId());
        if (playerDoc == null) return;
        Object[] list = playerDoc.getList(GamestatField.MEDALS.getQuery(), Document.class).toArray();
        List<Document> finalList = new ArrayList<>(list.length);
        for (int i = 0; i < list.length; i++) {
            Document doc = (Document) list[i];
            finalList.add(i, doc);
            if (doc.getString("medalName").equals(medal.getMedal().toString())) {
                finalList.remove(i);
                finalList.add(i, medal.createMedalDoc());
            }
        }
        mongoDB.updateGamestat(
            Filters.eq(DBFields.IDENTITY_ID, playerDoc.getString(DBFields.IDENTITY_ID)),
            Updates.set(GamestatField.MEDALS.getQuery(), finalList)
        );
    }

    public boolean alreadyHadMedal(Medals medal, Document doc) {
        List<Document> medals = doc.getList(GamestatField.MEDALS.getQuery(), Document.class);
        if (medals == null) return false;
        for (Document m : medals)
            if (medal.toString().equals(m.getString("medalName"))) return true;
        return false;
    }

    public void newMedalOnDataBase(Medal newMedal, Player player) {
        Document playerDoc = getPlayer(player.getUniqueId());
        if (playerDoc == null) return;
        mongoDB.updateGamestat(
            Filters.eq(DBFields.IDENTITY_ID, playerDoc.getString(DBFields.IDENTITY_ID)),
            Updates.addToSet(GamestatField.MEDALS.getQuery(), newMedal.createMedalDoc())
        );
    }

    public void newMedalOnDataBase(Document medalDoc, ServerPlayer sp) {
        mongoDB.updateGamestat(
            Filters.eq(DBFields.IDENTITY_ID, sp.getIdentityId()),
            Updates.addToSet(GamestatField.MEDALS.getQuery(), medalDoc)
        );
    }

    // -------------------------------------------------------------------------
    // Custom tags
    // -------------------------------------------------------------------------

    public void addCustomTag(UUID playerId, String tag) {
        Document playerDoc = getPlayer(playerId);
        if (playerDoc == null) return;
        mongoDB.updateGamestat(
            Filters.eq(DBFields.IDENTITY_ID, playerDoc.getString(DBFields.IDENTITY_ID)),
            Updates.addToSet(GamestatField.CUSTOMTAGS.getQuery(), tag)
        );
    }

    public void removeCustomTag(UUID playerId, String tag) {
        Document playerDoc = getPlayer(playerId);
        if (playerDoc == null) return;
        mongoDB.updateGamestat(
            Filters.eq(DBFields.IDENTITY_ID, playerDoc.getString(DBFields.IDENTITY_ID)),
            Updates.pull(GamestatField.CUSTOMTAGS.getQuery(), tag)
        );
    }

    // -------------------------------------------------------------------------
    // Display name
    // -------------------------------------------------------------------------

    public void setDisplayName(UUID playerId, String displayName, String listName) {
        Document playerDoc = getPlayer(playerId);
        if (playerDoc == null) return;
        mongoDB.updateGamestat(
            Filters.eq(DBFields.IDENTITY_ID, playerDoc.getString(DBFields.IDENTITY_ID)),
            Updates.combine(
                Updates.set(GamestatField.DISPLAYNAME.getQuery(), displayName),
                Updates.set(GamestatField.LISTNAME.getQuery(), listName)
            )
        );
    }

    public void clearDisplayName(UUID playerId) {
        Document playerDoc = getPlayer(playerId);
        if (playerDoc == null) return;
        mongoDB.updateGamestat(
            Filters.eq(DBFields.IDENTITY_ID, playerDoc.getString(DBFields.IDENTITY_ID)),
            Updates.combine(
                Updates.unset(GamestatField.DISPLAYNAME.getQuery()),
                Updates.unset(GamestatField.LISTNAME.getQuery())
            )
        );
    }

    // -------------------------------------------------------------------------
    // Generic update delegates
    // -------------------------------------------------------------------------

    public void updateStat(Bson filter, Bson update)      { mongoDB.updateGamestat(filter, update); }
    public void updateMultStats(Bson filter, Bson update) { mongoDB.updateManyGamestat(filter, update); }
    public void updateGamestat(Bson filter, Bson update)  { mongoDB.updateGamestat(filter, update); }
    public void deleteGamestat(Bson filter)               { mongoDB.deleteGamestat(filter); }
    public void updateIdentity(Bson filter, Bson update)  { mongoDB.updateIdentity(filter, update); }

    public DataBase getMongoDB()           { return mongoDB; }
    public FileConfiguration getConfig()   { return DataBase.getConfig(); }

    // -------------------------------------------------------------------------
    // Loading helpers
    // -------------------------------------------------------------------------

    public Medal[] loadMedals(List<Document> medals) {
        Medal[] newList = new Medal[Medals.values().length];
        for (Document doc : medals) {
            Medals medal = Medals.valueOf(doc.getString("medalName"));
            MLevel level = MLevel.valueOf(doc.getString("medalLevel"));
            newList[medal.getIndex()] = new Medal(medal, level);
        }
        return newList;
    }

    public HashMap<String, Mob> loadMobStats(List<Document> mobStats) {
        HashMap<String, Mob> map = new HashMap<>();
        if (mobStats == null) return map;
        for (Document doc : mobStats)
            map.put(doc.getString("mName"),
                new Mob(doc.getInteger("mId"), doc.getString("mName"), getLongOrZero(doc, "mNumKilled")));
        return map;
    }

    public HashMap<String, Block> loadBlockStats(List<Document> blockStats) {
        HashMap<String, Block> map = new HashMap<>();
        if (blockStats == null) return map;
        for (Document doc : blockStats)
            map.put(doc.getString("bName"),
                new Block(doc.getString("bName"), getLongOrZero(doc, "bNumDestroyed"), getLongOrZero(doc, "bNumPlaced")));
        return map;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Document enrichGamestatWithIdentity(Document gamestatDoc, Document identityDoc) {
        if (gamestatDoc == null) return null;
        if (identityDoc != null) {
            gamestatDoc.put(DBFields.NAME, identityDoc.getString(DBFields.USERNAME));
            gamestatDoc.put(DBFields.PLAYER_ID, UUID.fromString(identityDoc.getString(DBFields.EXTERNAL_ID)));
        }
        return gamestatDoc;
    }

    private long getLongOrZero(Document doc, String field) {
        Long val = doc.getLong(field);
        return val != null ? val : 0L;
    }

    private String getStringOrDefault(Document doc, String field, String defaultVal) {
        String val = doc.getString(field);
        return val != null ? val : defaultVal;
    }

    private List<Document> getListOrEmpty(Document doc, String field) {
        List<Document> list = doc.getList(field, Document.class);
        return list != null ? list : new ArrayList<>();
    }
}
