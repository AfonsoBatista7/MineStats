package org.rage.pluginstats.stats;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.rage.pluginstats.medals.Medal;
import org.rage.pluginstats.medals.Medals;
import org.rage.pluginstats.server.ServerManager;

/**
 * Top-level fields of the gamestats MongoDB document (not nested under stats.*).
 * @author Afonso Batista
 * 2021 - 2023
 */
public enum GamestatField {
    // query, display text, upload on save, firstValue
    STATUS(      "status",           "Is Online?",              true,  false),
    TIMEPLAYED(  "timePlayedMinutes","Time Played",              true,  0L),
    TIMEAFK(     "timeAFKMinutes",   "Time AFK",                true,  0L),
    LASTLOGIN(   "lastLogin",        "Last Login",              true,  new SimpleDateFormat("dd/MM/yyyy h:mm a").format(new Date())),
    PLAYERSINCE( "playerSince",      "Player Since",            false, new SimpleDateFormat("dd/MM/yyyy").format(new Date())),
    MEDALS(      "medals",           "Medals",                  false, Arrays.asList(new Medal(Medals.NOSTALGIAPLAYER).createMedalDoc())),
    CUSTOMTAGS(  "customTags",       "Custom Tags",             false, Arrays.asList()),
    VERSIONS(    "versionPlayed",    "Versions Played",         false, Arrays.asList(ServerManager.getServerVersion())),
    DISPLAYNAME( "displayName",      "Display Name",            false, null),
    LISTNAME(    "listName",         "List Name",               false, null);

    private final String query, text;
    private final boolean toUpload;
    private final Object firstValue;

    GamestatField(String query, String text, boolean toUpload, Object firstValue) {
        this.query = query;
        this.text = text;
        this.toUpload = toUpload;
        this.firstValue = firstValue;
    }

    public String getQuery()      { return query; }
    public String getText()       { return text; }
    public boolean toUpload()     { return toUpload; }
    public Object getFirstValue() { return firstValue; }
}
