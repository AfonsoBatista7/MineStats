package org.rage.pluginstats.mongoDB;

/**
 * MongoDB document field name constants.
 * Use these instead of raw string literals throughout the codebase.
 */
public final class DBFields {

    private DBFields() {}

    // -------------------------------------------------------------------------
    // Gamestat document fields
    // -------------------------------------------------------------------------
    public static final String IDENTITY_ID = "identityId";
    public static final String SERVER_ID   = "serverId";
    public static final String STATS       = "stats";

    // -------------------------------------------------------------------------
    // Identity document fields
    // -------------------------------------------------------------------------
    public static final String EXTERNAL_ID = "externalId";
    public static final String USERNAME    = "username";
    public static final String PROVIDER    = "provider";
    public static final String USER_ID     = "userId";

    // -------------------------------------------------------------------------
    // Synthetic fields added by DataBaseManager enrichment (not stored in DB)
    // -------------------------------------------------------------------------
    public static final String NAME      = "name";
    public static final String PLAYER_ID = "playerId";
}
