package org.rage.pluginstats.discord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.rage.pluginstats.mongoDB.DBFields;
import org.rage.pluginstats.mongoDB.DataBaseManager;
import org.rage.pluginstats.server.ServerManager;
import org.rage.pluginstats.utils.DiscordUtil;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;

/**
 * @author Afonso Batista
 * 2021 - 2023
 */
public class LinkManager {

    private Map<Integer, UUID> playerLinkCodes;
    private DataBaseManager mongoDB;
    private ServerManager serverMan;
    private final int RAND_RATIO = 10000;

    public LinkManager(DataBaseManager mongoDB, ServerManager serverMan) {
        playerLinkCodes = new HashMap<>();
        this.mongoDB = mongoDB;
        this.serverMan = serverMan;
    }

    public int generateNewCode(UUID playerId) {
        Random rand = new Random();
        int code = rand.nextInt(RAND_RATIO);

        Iterator<Entry<Integer, UUID>> it = playerLinkCodes.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, UUID> entry = it.next();
            if (entry.getValue().equals(playerId)) {
                playerLinkCodes.remove(entry.getKey());
                break;
            }
        }

        while (playerLinkCodes.containsKey(code))
            code = rand.nextInt(RAND_RATIO);

        playerLinkCodes.put(code, playerId);
        return code;
    }

    public UUID getPlayer(int code) { return playerLinkCodes.get(code); }

    public boolean hasGenCode(int code) { return playerLinkCodes.containsKey(code); }

    public String link(String rawCode, String discordUserId) {
        for (int code : playerLinkCodes.keySet()) {
            if (rawCode.contains(String.valueOf(code))) {
                String playerName = serverMan.getPlayerFromHashMap(getPlayer(code)).getName();
                linkProcess(code, discordUserId);
                return playerName;
            }
        }
        return "The code inserted dont match... try /link on minecraft to link your account";
    }

    public void unlink(UUID mcPlayerId) {
        Document mcIdentity = mongoDB.getMongoDB().getIdentityByExternalId(mcPlayerId.toString(), "minecraft");
        if (mcIdentity == null || mcIdentity.get(DBFields.USER_ID) == null) return;

        ObjectId oldUserId = mcIdentity.getObjectId(DBFields.USER_ID);
        Document discordIdentity = mongoDB.getMongoDB().getIdentityByUserId(oldUserId, "discord");

        // A user can group several identities (e.g. other linked MC alts), so unlinking
        // must only detach this one MC identity, not wipe out the shared user. Every
        // identity is expected to belong to a user, so give this one a fresh, separate one.
        Document newUserDoc = new Document();
        mongoDB.getMongoDB().insertUser(newUserDoc);

        mongoDB.updateIdentity(
            Filters.eq("_id", mcIdentity.getObjectId("_id")),
            Updates.set(DBFields.USER_ID, newUserDoc.getObjectId("_id"))
        );

        if (discordIdentity == null) return;

        // Only strip the linked role if no other Minecraft identity is still tied to
        // that Discord user's old user document — otherwise they're still linked.
        if (mongoDB.getMongoDB().hasIdentityByUserId(oldUserId, "minecraft")) return;

        Guild guild = DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId());
        if (guild != null)
            guild.removeRoleFromMember(
                discordIdentity.getString(DBFields.EXTERNAL_ID),
                guild.getRoleById(DiscordUtil.getRoleLinkedId())
            ).complete();
    }

    /**
     * Ensures a player's linked Discord user has the configured linked-role on this
     * server's guild. Called on join so players linked via another server also get
     * the role reapplied here.
     */
    public void syncLinkedRole(UUID mcPlayerId) {
        Document mcIdentity = mongoDB.getMongoDB().getIdentityByExternalId(mcPlayerId.toString(), "minecraft");
        if (mcIdentity == null || mcIdentity.get(DBFields.USER_ID) == null) return;

        Document discordIdentity = mongoDB.getMongoDB().getIdentityByUserId(mcIdentity.getObjectId(DBFields.USER_ID), "discord");
        if (discordIdentity == null) return;

        Guild guild = DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId());
        if (guild == null) return;

        Role role = guild.getRoleById(DiscordUtil.getRoleLinkedId());
        if (role == null) return;

        guild.addRoleToMember(discordIdentity.getString(DBFields.EXTERNAL_ID), role).queue();
    }

    private void linkProcess(int code, String discordUserId) {
        UUID mcUUID = getPlayer(code);

        Document mcIdentity      = mongoDB.getMongoDB().getIdentityByExternalId(mcUUID.toString(), "minecraft");
        Document discordIdentity = mongoDB.getMongoDB().getIdentityByExternalId(discordUserId, "discord");

        if (mcIdentity == null || discordIdentity == null) {
            playerLinkCodes.remove(code);
            return;
        }

        ObjectId userId = null;
        if (mcIdentity.get(DBFields.USER_ID) != null)
            userId = mcIdentity.getObjectId(DBFields.USER_ID);
        else if (discordIdentity.get(DBFields.USER_ID) != null)
            userId = discordIdentity.getObjectId(DBFields.USER_ID);
        else {
            Document userDoc = new Document();
            mongoDB.getMongoDB().insertUser(userDoc);
            userId = userDoc.getObjectId("_id");
        }

        mongoDB.updateIdentity(Filters.eq("_id", mcIdentity.getObjectId("_id")),      Updates.set(DBFields.USER_ID, userId));
        mongoDB.updateIdentity(Filters.eq("_id", discordIdentity.getObjectId("_id")), Updates.set(DBFields.USER_ID, userId));

        playerLinkCodes.remove(code);

        Guild guild = DiscordUtil.getJda().getGuildById(DiscordUtil.getGuildId());
        if (guild != null)
            guild.addRoleToMember(discordUserId, guild.getRoleById(DiscordUtil.getRoleLinkedId())).complete();
    }
}
