package de.davidvogt.hkbmod.research;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.util.NBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages persistent storage of player research data
 */
public class PlayerResearchDataManager {

    private static final String NBT_KEY = "hkbmod_research_data";
    private static final Map<UUID, PlayerResearchData> RESEARCH_DATA_CACHE = new HashMap<>();

    /**
     * Get or create research data for a player
     */
    public static PlayerResearchData getPlayerResearchData(Player player) {
        UUID playerId = player.getUUID();

        // Check cache first
        PlayerResearchData data = RESEARCH_DATA_CACHE.get(playerId);
        if (data != null) {
            return data;
        }

        // Load from player NBT data
        data = loadFromPlayer(player);
        RESEARCH_DATA_CACHE.put(playerId, data);

        HkbMod.LOGGER.info("[PlayerResearchDataManager] Loaded research data for player: " + player.getName().getString() +
            " - Unlocked researches: " + data.getUnlockedResearches().size());

        return data;
    }

    /**
     * Save research data for a player
     */
    public static void savePlayerResearchData(Player player, PlayerResearchData data) {
        UUID playerId = player.getUUID();

        // Update cache
        RESEARCH_DATA_CACHE.put(playerId, data);

        // Save to player NBT if on server side
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            saveToPlayer(serverPlayer, data);
            HkbMod.LOGGER.info("[PlayerResearchDataManager] Saved research data for player: " + player.getName().getString() +
                " - Unlocked researches: " + data.getUnlockedResearches().size());
        }
    }

    /**
     * Load research data from player's persistent data
     */
    private static PlayerResearchData loadFromPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag persistentData = serverPlayer.getPersistentData();
            if (persistentData.contains(NBT_KEY)) {
                CompoundTag researchTag = NBTUtil.getCompoundTag(persistentData, NBT_KEY);
                PlayerResearchData data = new PlayerResearchData();
                data.deserializeNBT(researchTag);
                return data;
            }
        }

        // Return new data if no saved data exists
        return new PlayerResearchData();
    }

    /**
     * Save research data to player's persistent data
     */
    private static void saveToPlayer(ServerPlayer player, PlayerResearchData data) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag researchTag = data.serializeNBT();
        persistentData.put(NBT_KEY, researchTag);
    }

    /**
     * Clear cached data for a player (called when player logs out)
     */
    public static void clearCache(UUID playerId) {
        RESEARCH_DATA_CACHE.remove(playerId);
        HkbMod.LOGGER.info("[PlayerResearchDataManager] Cleared cache for player: " + playerId);
    }

    /**
     * Get cached data without loading (for client-side sync)
     */
    public static PlayerResearchData getCachedData(UUID playerId) {
        return RESEARCH_DATA_CACHE.get(playerId);
    }
}