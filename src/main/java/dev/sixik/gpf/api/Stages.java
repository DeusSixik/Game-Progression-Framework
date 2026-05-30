package dev.sixik.gpf.api;

import dev.sixik.gpf.api.progression.PlayerProgression;
import dev.sixik.gpf.impl.server.PlayerStageDataService;
import dev.sixik.gpf.impl.server.ServerPlayerProgression;
import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.level.ServerPlayer;

/**
 * Main public entry point for working with stage definitions and player progression.
 */
public final class Stages {

    private Stages() {
    }

    /**
     * Creates a progression view for the provided player.
     *
     * @param player the player to wrap
     * @return a progression facade backed by the current server-side storage
     */
    public static PlayerProgression player(ServerPlayer player) {
        return new ServerPlayerProgression(player);
    }

    /**
     * Resolves a validated stage name to its persistent id.
     *
     * @param stageName the normalized stage name
     * @return the persistent stage id
     */
    public static short getStageId(String stageName) {
        return StagesRegistry.INSTANCE.getIdOrThrow(stageName);
    }

    /**
     * Resolves a normalized stage name without validating it first.
     * Use this only in hot paths where the input is already trusted.
     *
     * @param normalizedStageName the already normalized stage name
     * @return the persistent stage id, or {@link StagesRegistry#UNKNOWN_STAGE} when absent
     */
    public static short getStageIdFast(String normalizedStageName) {
        return StagesRegistry.INSTANCE.getIdFast(normalizedStageName);
    }

    /**
     * Resolves a stage id to its registered name.
     *
     * @param stageId the stage id to resolve
     * @return the normalized stage name
     */
    public static String getStageName(short stageId) {
        String stageName = StagesRegistry.INSTANCE.getName(stageId);
        if (stageName == null) {
            throw new IllegalArgumentException("Unknown stage id '" + stageId + "'");
        }

        return stageName;
    }

    /**
     * Resolves a stage id without throwing when it is unknown.
     *
     * @param stageId the stage id to resolve
     * @return the normalized stage name, or {@code null} when unknown
     */
    public static String getStageNameOrNull(short stageId) {
        return StagesRegistry.INSTANCE.getName(stageId);
    }

    /**
     * Checks whether a validated stage name exists in the registry.
     *
     * @param stageName the normalized stage name
     * @return {@code true} when the stage exists
     */
    public static boolean isKnownStage(String stageName) {
        return StagesRegistry.INSTANCE.isKnownStage(stageName);
    }

    /**
     * Checks whether a normalized stage name exists in the registry without validating input.
     *
     * @param normalizedStageName the already normalized stage name
     * @return {@code true} when the stage exists
     */
    public static boolean isKnownStageFast(String normalizedStageName) {
        return StagesRegistry.INSTANCE.isKnownStageFast(normalizedStageName);
    }

    /**
     * Checks whether a validated stage is active in the current reload result.
     *
     * @param stageName the normalized stage name
     * @return {@code true} when the stage is active
     */
    public static boolean isActiveStage(String stageName) {
        return StagesRegistry.INSTANCE.isActiveStage(stageName);
    }

    /**
     * Checks whether a normalized stage is active without validating input.
     *
     * @param normalizedStageName the already normalized stage name
     * @return {@code true} when the stage is active
     */
    public static boolean isActiveStageFast(String normalizedStageName) {
        return StagesRegistry.INSTANCE.isActiveStageFast(normalizedStageName);
    }

    /**
     * Checks whether a stage id exists in the registry.
     *
     * @param stageId the stage id to check
     * @return {@code true} when the stage id is known
     */
    public static boolean isKnownStageId(short stageId) {
        return StagesRegistry.INSTANCE.isKnownStageId(stageId);
    }

    /**
     * Resolves a stage name and checks whether the player has it.
     *
     * @param stageName the normalized stage name
     * @param player the player to query
     * @return {@code true} when the player has the stage
     */
    public static boolean hasStageSlow(String stageName, ServerPlayer player) {
        return hasStage(getStageId(stageName), player);
    }

    /**
     * Checks whether the player has the provided stage id.
     *
     * @param stageId the stage id to check
     * @param player the player to query
     * @return {@code true} when the player has the stage
     */
    public static boolean hasStage(short stageId, ServerPlayer player) {
        requireKnownStageId(stageId);
        return hasStageFast(stageId, player);
    }

    /**
     * Resolves all provided stage names and checks whether the player has every one of them.
     *
     * @param player the player to query
     * @param stageNames the normalized stage names to check
     * @return {@code true} when the player has all provided stages
     */
    public static boolean hasStagesSlow(ServerPlayer player, String... stageNames) {
        short[] stageIds = new short[stageNames.length];
        for (int i = 0; i < stageNames.length; i++) {
            stageIds[i] = getStageId(stageNames[i]);
        }

        return hasStages(player, stageIds);
    }

    /**
     * Checks whether the player has every provided stage id.
     *
     * @param player the player to query
     * @param stageIds the stage ids to check
     * @return {@code true} when the player has all provided stages
     */
    public static boolean hasStages(ServerPlayer player, short... stageIds) {
        for (short stageId : stageIds) {
            requireKnownStageId(stageId);
        }

        return hasStagesFast(player, stageIds);
    }

    /**
     * Checks whether the player has every provided trusted stage id without validating them first.
     *
     * @param player the player to query
     * @param stageIds the trusted stage ids to check
     * @return {@code true} when the player has all provided stages
     */
    public static boolean hasStagesFast(ServerPlayer player, short... stageIds) {
        return PlayerStageDataService.hasStages(player, stageIds);
    }

    /**
     * Checks whether the player has the provided stage id without validating it first.
     *
     * @param stageId the trusted stage id to check
     * @param player the player to query
     * @return {@code true} when the player has the stage
     */
    public static boolean hasStageFast(short stageId, ServerPlayer player) {
        return PlayerStageDataService.hasStage(player, stageId);
    }

    /**
     * Resolves a stage name and adds it to the player.
     *
     * @param stageName the normalized stage name
     * @param player the target player
     * @return {@code true} when the player gained the stage
     */
    public static boolean addStageSlow(String stageName, ServerPlayer player) {
        return addStage(getStageId(stageName), player);
    }

    /**
     * Adds a stage to the player.
     *
     * @param stageId the stage id to add
     * @param player the target player
     * @return {@code true} when the player gained the stage
     */
    public static boolean addStage(short stageId, ServerPlayer player) {
        requireKnownStageId(stageId);
        return addStageFast(stageId, player);
    }

    /**
     * Adds a trusted stage id to the player without validating it first.
     *
     * @param stageId the trusted stage id to add
     * @param player the target player
     * @return {@code true} when the player gained the stage
     */
    public static boolean addStageFast(short stageId, ServerPlayer player) {
        return PlayerStageDataService.addStage(player, stageId);
    }

    /**
     * Resolves a stage name and removes it from the player.
     *
     * @param stageName the normalized stage name
     * @param player the target player
     * @return {@code true} when the player lost the stage
     */
    public static boolean removeStageSlow(String stageName, ServerPlayer player) {
        return removeStage(getStageId(stageName), player);
    }

    /**
     * Removes a stage from the player.
     *
     * @param stageId the stage id to remove
     * @param player the target player
     * @return {@code true} when the player lost the stage
     */
    public static boolean removeStage(short stageId, ServerPlayer player) {
        requireKnownStageId(stageId);
        return removeStageFast(stageId, player);
    }

    /**
     * Removes a trusted stage id from the player without validating it first.
     *
     * @param stageId the trusted stage id to remove
     * @param player the target player
     * @return {@code true} when the player lost the stage
     */
    public static boolean removeStageFast(short stageId, ServerPlayer player) {
        return PlayerStageDataService.removeStage(player, stageId);
    }

    /**
     * Clears all stages from the player.
     *
     * @param player the target player
     * @return {@code true} when any stage was removed
     */
    public static boolean clearStages(ServerPlayer player) {
        return PlayerStageDataService.clearStages(player);
    }

    /**
     * Replaces the player's stages with the provided stage container.
     *
     * @param player the target player
     * @param stageData the new stage contents
     * @return {@code true} when the player's stages changed
     */
    public static boolean setStages(ServerPlayer player, StageData stageData) {
        return PlayerStageDataService.setStages(player, stageData);
    }

    /**
     * Merges the provided stages into the player.
     *
     * @param player the target player
     * @param stageData the stage contents to merge
     * @return {@code true} when the player's stages changed
     */
    public static boolean mergeStages(ServerPlayer player, StageData stageData) {
        return PlayerStageDataService.mergeStages(player, stageData);
    }

    /**
     * Replaces the player's stages with a raw bitset representation.
     *
     * @param player the target player
     * @param rawStages the raw stage bitset
     * @return {@code true} when the player's stages changed
     */
    public static boolean replaceRawStages(ServerPlayer player, long[] rawStages) {
        return PlayerStageDataService.replace(player, rawStages);
    }

    /**
     * Copies the player's stages into an immutable snapshot object.
     *
     * @param player the player to snapshot
     * @return a detached copy of the player's current stages
     */
    public static StageData snapshot(ServerPlayer player) {
        return PlayerStageDataService.copy(player);
    }

    private static void requireKnownStageId(short stageId) {
        if (!isKnownStageId(stageId)) {
            throw new IllegalArgumentException("Unknown stage id '" + stageId + "'");
        }
    }
}
