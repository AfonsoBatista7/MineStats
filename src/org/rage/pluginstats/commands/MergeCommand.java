package org.rage.pluginstats.commands;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.mongoDB.DBFields;
import org.rage.pluginstats.stats.GamestatField;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.utils.Util;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

/**
 * If a player has changed their name, use this command to merge two player profiles.
 * @author Afonso Batista
 * 2021 - 2023
 */
public class MergeCommand implements CommandExecutor {

    private DataBaseManager mongoDB;
    private ServerManager serverMan;

    public MergeCommand(DataBaseManager mongoDB, ServerManager serverMan) {
        this.mongoDB = mongoDB;
        this.serverMan = serverMan;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage(Util.chat("&b[MineStats]&7 - Only the console can do this command..."));
            return false;
        }

        if (args.length != 2) {
            sender.sendMessage(Util.chat("&b[MineStats]&7 - Specify two player names to merge."));
            return false;
        }

        Document playerDoc1 = mongoDB.getPlayerByName(args[0]);
        Document playerDoc2 = mongoDB.getPlayerByName(args[1]);

        if (playerDoc1 == null || playerDoc2 == null) {
            sender.sendMessage(Util.chat("&b[MineStats]&7 - One or more players doesn't exist on DataBase."));
            return false;
        }

        if (playerDoc1.equals(playerDoc2)) {
            sender.sendMessage(Util.chat("&b[MineStats]&7 - The two players are the same :/ ."));
            return false;
        }

        Document recentPlayer = getRecentPlayer(playerDoc1, playerDoc2);
        Document oldPlayer    = recentPlayer.equals(playerDoc1) ? playerDoc2 : playerDoc1;

        try {
            mergePlayerDocs(recentPlayer, oldPlayer);
        } catch (Exception e) {
            Bukkit.broadcastMessage(Util.chat("&b[MineStats]&7 - An ERROR occurred while merging..."));
            e.printStackTrace();
            return false;
        }

        serverMan.deleteFromHashMap((UUID) playerDoc1.get(DBFields.PLAYER_ID));
        serverMan.deleteFromHashMap((UUID) playerDoc2.get(DBFields.PLAYER_ID));

        UUID playerId = (UUID) recentPlayer.get(DBFields.PLAYER_ID);
        Document merged = mongoDB.getPlayer(playerId);

        try {
            mongoDB.downloadFromDataBase(new ServerPlayer(playerId, mongoDB), merged);
        } catch (ParseException e) {
            Bukkit.broadcastMessage(Util.chat("&b[MineStats]&7 - An ERROR occurred while merging..."));
            e.printStackTrace();
            return false;
        }

        Bukkit.broadcastMessage(Util.chat(
            "&b[MineStats]&7 - Player &a<p1>&7 and &a<p2>&7 now are one B)."
                .replace("<p1>", merged.getString(DBFields.NAME))
                .replace("<p2>", playerDoc2.getString(DBFields.NAME))));

        return true;
    }

    private void mergePlayerDocs(Document recentPlayer, Document oldPlayer) {
        String recentId = recentPlayer.getString(DBFields.IDENTITY_ID);

        // Merge list fields
        mongoDB.updateGamestat(Filters.eq(DBFields.IDENTITY_ID, recentId),
            Updates.combine(
                Updates.set(GamestatField.CUSTOMTAGS.getQuery(),
                    mergeTagData(
                        recentPlayer.getList(GamestatField.CUSTOMTAGS.getQuery(), String.class),
                        oldPlayer.getList(GamestatField.CUSTOMTAGS.getQuery(), String.class))),
                Updates.set(Stats.BLOCKS.getDbPath(),
                    mergeBlockData(
                        getNestedList(recentPlayer, Stats.BLOCKS.getQuery()),
                        getNestedList(oldPlayer, Stats.BLOCKS.getQuery()))),
                Updates.set(Stats.MOBSKILLED.getDbPath(),
                    mergeMobData(
                        getNestedList(recentPlayer, Stats.MOBSKILLED.getQuery()),
                        getNestedList(oldPlayer, Stats.MOBSKILLED.getQuery()))),
                Updates.set(GamestatField.MEDALS.getQuery(),
                    mergeMedalData(
                        recentPlayer.getList(GamestatField.MEDALS.getQuery(), Document.class),
                        oldPlayer.getList(GamestatField.MEDALS.getQuery(), Document.class)))
            ));

        // Merge numeric fields
        mongoDB.updateGamestat(Filters.eq(DBFields.IDENTITY_ID, recentId),
            Updates.combine(
                Updates.set(GamestatField.STATUS.getQuery(),
                    boolOrFalse(recentPlayer, GamestatField.STATUS.getQuery()) ||
                    boolOrFalse(oldPlayer,    GamestatField.STATUS.getQuery())),
                Updates.inc(Stats.BLOCKSDEST.getDbPath(),   getNestedLong(oldPlayer, Stats.BLOCKSDEST.getQuery())),
                Updates.inc(Stats.BLOCKSPLA.getDbPath(),    getNestedLong(oldPlayer, Stats.BLOCKSPLA.getQuery())),
                Updates.inc(Stats.BLOCKSMINED.getDbPath(),  getNestedLong(oldPlayer, Stats.BLOCKSMINED.getQuery())),
                Updates.inc(Stats.KILLS.getDbPath(),        getNestedLong(oldPlayer, Stats.KILLS.getQuery())),
                Updates.inc(Stats.MOBKILLS.getDbPath(),     getNestedLong(oldPlayer, Stats.MOBKILLS.getQuery())),
                Updates.inc(Stats.TRAVELLED.getDbPath(),    getNestedLong(oldPlayer, Stats.TRAVELLED.getQuery())),
                Updates.inc(Stats.DEATHS.getDbPath(),       getNestedLong(oldPlayer, Stats.DEATHS.getQuery())),
                Updates.inc(Stats.TIMESLOGIN.getDbPath(),   getNestedLong(oldPlayer, Stats.TIMESLOGIN.getQuery())),
                Updates.inc(Stats.FISHCAUGHT.getDbPath(),   getNestedLong(oldPlayer, Stats.FISHCAUGHT.getQuery())),
                Updates.inc(Stats.REDSTONEUSED.getDbPath(), getNestedLong(oldPlayer, Stats.REDSTONEUSED.getQuery())),
                Updates.min(GamestatField.PLAYERSINCE.getQuery(),
                    oldPlayer.getString(GamestatField.PLAYERSINCE.getQuery())),
                Updates.set(GamestatField.TIMEPLAYED.getQuery(),
                    getLongOrZero(recentPlayer, GamestatField.TIMEPLAYED.getQuery()) +
                    getLongOrZero(oldPlayer,    GamestatField.TIMEPLAYED.getQuery())),
                Updates.addEachToSet(GamestatField.MEDALS.getQuery(),
                    getListOrEmpty(oldPlayer, GamestatField.MEDALS.getQuery())),
                Updates.addEachToSet(GamestatField.VERSIONS.getQuery(),
                    oldPlayer.getList(GamestatField.VERSIONS.getQuery(), String.class) != null
                        ? oldPlayer.getList(GamestatField.VERSIONS.getQuery(), String.class)
                        : new ArrayList<String>())
            ));

        mongoDB.deleteGamestat(Filters.eq(DBFields.IDENTITY_ID, oldPlayer.getString(DBFields.IDENTITY_ID)));
    }

    // -------------------------------------------------------------------------
    // List merge helpers
    // -------------------------------------------------------------------------

    private List<String> mergeTagData(List<String> rpTags, List<String> opTags) {
        List<String> merged = new ArrayList<>(rpTags != null ? rpTags : new ArrayList<>());
        if (opTags != null)
            for (String tag : opTags)
                if (!merged.contains(tag)) merged.add(tag);
        return merged;
    }

    private List<Document> mergeBlockData(List<Document> rpBlocks, List<Document> opBlocks) {
        int toRemove = -1;
        for (Document rDoc : rpBlocks) {
            for (Document oDoc : opBlocks) {
                if (rDoc.getString("bName").equals(oDoc.getString("bName"))) {
                    rDoc.put("bNumPlaced",    rDoc.getLong("bNumPlaced")    + oDoc.getLong("bNumPlaced"));
                    rDoc.put("bNumDestroyed", rDoc.getLong("bNumDestroyed") + oDoc.getLong("bNumDestroyed"));
                    toRemove = opBlocks.indexOf(oDoc);
                }
            }
            if (toRemove != -1) { opBlocks.remove(toRemove); toRemove = -1; }
        }
        rpBlocks.addAll(opBlocks);
        return rpBlocks;
    }

    private List<Document> mergeMobData(List<Document> rpMobs, List<Document> opMobs) {
        int toRemove = -1;
        for (Document rDoc : rpMobs) {
            for (Document oDoc : opMobs) {
                if (rDoc.getString("mName").equals(oDoc.getString("mName"))) {
                    rDoc.put("mNumKilled", rDoc.getLong("mNumKilled") + oDoc.getLong("mNumKilled"));
                    toRemove = opMobs.indexOf(oDoc);
                }
            }
            if (toRemove != -1) { opMobs.remove(toRemove); toRemove = -1; }
        }
        rpMobs.addAll(opMobs);
        return rpMobs;
    }

    private List<Document> mergeMedalData(List<Document> recentMedals, List<Document> oldMedals) {
        List<Document> newList = new ArrayList<>(recentMedals);
        for (Document rDoc : recentMedals) {
            for (Document oDoc : oldMedals) {
                if (rDoc.getString("medalName").equals(oDoc.getString("medalName"))) {
                    MLevel lvl1 = MLevel.valueOf(rDoc.getString("medalLevel"));
                    MLevel lvl2 = MLevel.valueOf(oDoc.getString("medalLevel"));
                    if (lvl1.getNumber() < lvl2.getNumber()) {
                        newList.remove(rDoc);
                        rDoc.put("medalLevel", lvl2.toString());
                        newList.add(rDoc);
                    }
                }
            }
        }
        return newList;
    }

    private Document getRecentPlayer(Document p1, Document p2) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy h:mm a");
            return fmt.parse(p1.getString(GamestatField.LASTLOGIN.getQuery()))
                      .compareTo(fmt.parse(p2.getString(GamestatField.LASTLOGIN.getQuery()))) > 0 ? p1 : p2;
        } catch (ParseException e) {
            System.out.println("[MineStats] - Error parsing last login dates during merge.");
        }
        return p1;
    }

    // -------------------------------------------------------------------------
    // Document reading helpers
    // -------------------------------------------------------------------------

    private List<Document> getNestedList(Document doc, String field) {
        Document statsDoc = doc.get(DBFields.STATS, Document.class);
        if (statsDoc == null) return new ArrayList<>();
        List<Document> list = statsDoc.getList(field, Document.class);
        return list != null ? list : new ArrayList<>();
    }

    private long getNestedLong(Document doc, String field) {
        Document statsDoc = doc.get(DBFields.STATS, Document.class);
        if (statsDoc == null) return 0L;
        Long val = statsDoc.getLong(field);
        return val != null ? val : 0L;
    }

    private long getLongOrZero(Document doc, String field) {
        Long val = doc.getLong(field);
        return val != null ? val : 0L;
    }

    private boolean boolOrFalse(Document doc, String field) {
        Boolean val = doc.getBoolean(field);
        return val != null && val;
    }

    private List<Document> getListOrEmpty(Document doc, String field) {
        List<Document> list = doc.getList(field, Document.class);
        return list != null ? list : new ArrayList<>();
    }
}
