package org.rage.pluginstats.stats;

import java.util.Arrays;

/**
 * Gameplay stats stored inside the gamestats.stats subdocument in MongoDB.
 * @author Afonso Batista
 * 2021 - 2023
 */
public enum Stats {
    // index, query (MongoDB field name), display text, printable, firstValue
    BLOCKSDEST(0,  "blcksDestroyed",   "Blocks Destroyed",      true,  0L),
    BLOCKSPLA( 1,  "blcksPlaced",      "Blocks Placed",         true,  0L),
    BLOCKSMINED(2, "blockMined",       "Blocks Mined",          true,  0L),
    KILLS(     3,  "kills",            "Kills",                 true,  0L),
    MOBKILLS(  4,  "mobKills",         "Monster Kills",         true,  0L),
    TRAVELLED( 5,  "mTravelled",       "Meters Travelled",      true,  0L),
    DEATHS(    6,  "deaths",           "Deaths",                true,  0L),
    REDSTONEUSED(7,"redstoneUsed",     "Redstone Used",         true,  0L),
    FISHCAUGHT(8,  "fishCaught",       "Fish Caught",           true,  0L),
    ENDERDRAGONKILLS(9, "enderdragonKills", "Ender Dragon Kills", true, 0L),
    WITHERKILLS(10,"witherKills",      "Wither Kills",          true,  0L),
    TIMESLOGIN(11, "timeslogin",       "Number of Logins",      true,  0L),
    MOBSKILLED(12, "mobsKilled",       "Mobs Killed",           false, Arrays.asList()),
    BLOCKS(    13, "blocks",           "Blocks",                false, Arrays.asList());

    private final int index;
    private final String query, text;
    private final boolean print;
    private final Object firstValue;

    Stats(int index, String query, String text, boolean print, Object firstValue) {
        this.index = index;
        this.query = query;
        this.text = text;
        this.print = print;
        this.firstValue = firstValue;
    }

    public int getIndex()         { return index; }
    public String getQuery()      { return query; }
    /** Full MongoDB path — always "stats.<field>" since all Stats live in the stats subdocument. */
    public String getDbPath()     { return "stats." + query; }
    public String getText()       { return text; }
    public boolean toPrint()      { return print; }
    public Object getFirstValue() { return firstValue; }
}
