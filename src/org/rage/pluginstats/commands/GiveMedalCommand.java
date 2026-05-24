package org.rage.pluginstats.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.Main;
import org.rage.pluginstats.medals.MLevel;
import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.mongoDB.DBFields;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.GamestatField;
import org.rage.pluginstats.utils.Util;

/**
 * Gives a specified medal to a specified player.
 * @author Afonso Batista
 * 2021 - 2023
 */
public class GiveMedalCommand implements CommandExecutor {

    private DataBaseManager mongoDB;
    private ServerManager serverMan;

    public GiveMedalCommand(DataBaseManager mongoDB, ServerManager serverMan) {
        this.mongoDB = mongoDB;
        this.serverMan = serverMan;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage(Util.chat("&b[MineStats]&7 - Only the console can do this command..."));
            return false;
        }

        Document playerDoc;
        try {
            playerDoc = mongoDB.getPlayerByName(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a player."));
            return false;
        }

        if (playerDoc == null) {
            sender.sendMessage(Util.chat("&b[MineStats]&7 - This player doesn't exist on DataBase."));
            return false;
        }

        Medals medal;
        try {
            medal = Medals.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Util.chat("&b[MineStats]&7 - This Medal doesn't exist yet."));
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a medal."));
            return false;
        }

        MLevel level;
        try {
            level = MLevel.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Util.chat("&b[MineStats]&7 - This Level doesn't exist yet."));
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            level = MLevel.GOD;
        }

        UUID playerId = (UUID) playerDoc.get(DBFields.PLAYER_ID);
        ServerPlayer sp = serverMan.getPlayerFromHashMap(playerId);

        if (sp == null) {
            try {
                sp = new ServerPlayer(playerId, mongoDB);
                mongoDB.downloadFromDataBase(sp, playerDoc);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Medal newMedal = new Medal(medal, level);
        Document newMedalDoc = newMedal.createMedalDoc();

        if (sp.haveMedal(medal)) {
            if (sp.getMedalByMedal(medal).getMedalLevel().equals(level)) {
                sender.sendMessage(Util.chat("&b[MineStats]&7 - This player already have this medal."));
                return false;
            } else {
                Object[] list = playerDoc.getList(GamestatField.MEDALS.getQuery(), Document.class).toArray();
                List<Document> finalList = new ArrayList<>(list.length);
                for (int i = 0; i < list.length; i++) {
                    Document document = (Document) list[i];
                    finalList.add(i, document);
                    if (document.getString("medalName").equals(medal.toString())) {
                        finalList.remove(i);
                        finalList.add(i, newMedalDoc);
                    }
                }
            }
        } else {
            sp.newMedal(newMedal);
            mongoDB.newMedalOnDataBase(newMedalDoc, sp);
        }

        Player player = Main.currentServer.getPlayer(playerDoc.getString(DBFields.NAME));
        if (player != null) newMedal.newMedalEffect(player);

        Bukkit.broadcastMessage(Util.chat(
            "&b[MineStats]&7 - Now, &a<player>&7 have the &c<medal>&7 &6<level>&7 level Medal!."
                .replace("<player>", playerDoc.getString(DBFields.NAME))
                .replace("<medal>",  newMedal.getMedal().toString())
                .replace("<level>",  newMedal.getMedalLevel().toString())));

        return true;
    }
}
