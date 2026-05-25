package org.rage.pluginstats.commands;

import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.rage.pluginstats.mongoDB.DBFields;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.player.ServerPlayer;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.stats.GamestatField;
import org.rage.pluginstats.stats.Stats;
import org.rage.pluginstats.utils.Util;

/**
 * Prints all current stats for a specified player.
 * @author Afonso Batista
 * 2021 - 2023
 */
public class PlayerStatsCommand implements CommandExecutor {

    private DataBaseManager mongoDB;
    private ServerManager serverMan;

    public PlayerStatsCommand(DataBaseManager mongoDB, ServerManager serverMan) {
        this.serverMan = serverMan;
        this.mongoDB = mongoDB;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        String name = sender.getName();

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Util.chat("&b[MineStats]&7 - You need to specify a player."));
                return false;
            }
        } else {
            name = args[0];
        }

        Document playerDoc = mongoDB.getPlayerByName(name);

        if (playerDoc == null) {
            Collection<? extends Player> players = sender.getServer().getOnlinePlayers();
            Iterator<? extends Player> iterator = players.iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();
                if (sender.getName().equals(player.getName())) {
                    playerDoc = mongoDB.getPlayer(player.getUniqueId());
                    break;
                }
            }
            if (playerDoc == null) {
                sender.sendMessage(Util.chat("&b[MineStats]&7 - You don't exist on DataBase."));
                return false;
            }
        }

        UUID playerId = (UUID) playerDoc.get(DBFields.PLAYER_ID);

        ServerPlayer pp = serverMan.getPlayerFromHashMap(playerId);
        if (pp == null) {
            pp = new ServerPlayer(playerId, mongoDB);
            try {
                mongoDB.downloadFromDataBase(pp, playerDoc);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        sender.sendMessage(Util.chat("&b[MineStats]&7 - &c<player>&7 Stats:").replace("<player>", name));

        // Gameplay stats (nested under stats.*)
        for (Stats stat : Stats.values()) {
            if (stat.toPrint())
                sender.sendMessage(Util.chat("    &e<stat>&7: &b<value>")
                    .replace("<stat>",  stat.getText())
                    .replace("<value>", String.valueOf(Util.getStatVariable(pp, stat))));
        }

        // Top-level time fields shown as human-readable timestamps
        sender.sendMessage(Util.chat("    &e<stat>&7: &b<value>")
            .replace("<stat>",  GamestatField.TIMEPLAYED.getText())
            .replace("<value>", Util.minutesToTimestamp(pp.getTimePlayed())));
        sender.sendMessage(Util.chat("    &e<stat>&7: &b<value>")
            .replace("<stat>",  GamestatField.TIMEAFK.getText())
            .replace("<value>", Util.minutesToTimestamp(pp.getTimeAFK())));

        Document discUser = mongoDB.getDiscordUserByPlayer(playerId);
        String linkLine = discUser != null
            ? Util.chat("    &a&lLink&7: &b&l<value>").replace("<value>", discUser.getString(DBFields.USERNAME))
            : Util.chat("    &c&lLink&7: &b&l???");
        sender.sendMessage(linkLine);

        return true;
    }
}
