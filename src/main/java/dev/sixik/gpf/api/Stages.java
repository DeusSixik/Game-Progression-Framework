package dev.sixik.gpf.api;

import dev.sixik.gpf.api.progression.PlayerProgression;
import dev.sixik.gpf.impl.server.PlayerStageDataService;
import dev.sixik.gpf.impl.server.ServerPlayerProgression;
import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Main public entry point for working with stage definitions and player progression.
 */
public final class Stages {

    private Stages() {
    }

    /**
     * Creates a progression view for the provided player id.
     *
     * @param playerId the owner id to wrap
     * @return a progression facade backed by the current server-side storage
     */
    public static PlayerProgression player(UUID playerId) {
        return new ServerPlayerProgression(playerId);
    }

    /**
     * Creates a progression view for the provided player.
     *
     * @param player the player to wrap
     * @return a progression facade backed by the current server-side storage
     */
    public static PlayerProgression player(ServerPlayer player) {
        return player(player.getUUID());
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
     * Resolves a stage name and checks whether the owner has it.
     *
     * @param ownerId the owner to query
     * @param stageName the normalized stage name
     * @return {@code true} when the owner has the stage
     */
    public static boolean hasStageSlow(UUID ownerId, String stageName) {
        return hasStage(ownerId, getStageId(stageName));
    }

    /**
     * Checks whether the owner has the provided stage id.
     *
     * @param ownerId the owner to query
     * @param stageId the stage id to check
     * @return {@code true} when the owner has the stage
     */
    public static boolean hasStage(UUID ownerId, short stageId) {
        requireKnownStageId(stageId);
        return hasStageFast(ownerId, stageId);
    }

    /**
     * Resolves all provided stage names and checks whether the owner has every one of them.
     *
     * @param ownerId the owner to query
     * @param stageNames the normalized stage names to check
     * @return {@code true} when the owner has all provided stages
     */
    public static boolean hasStagesSlow(UUID ownerId, String... stageNames) {
        return hasStages(ownerId, resolveStageIds(stageNames));
    }

    /**
     * Checks whether the owner has every provided stage id.
     *
     * @param ownerId the owner to query
     * @param stageIds the stage ids to check
     * @return {@code true} when the owner has all provided stages
     */
    public static boolean hasStages(UUID ownerId, short... stageIds) {
        requireKnownStageIds(stageIds);
        return hasStagesFast(ownerId, stageIds);
    }

    /**
     * Checks whether the owner has every provided trusted stage id without validating them first.
     *
     * @param ownerId the owner to query
     * @param stageIds the trusted stage ids to check
     * @return {@code true} when the owner has all provided stages
     */
    public static boolean hasStagesFast(UUID ownerId, short... stageIds) {
        return PlayerStageDataService.hasStages(ownerId, stageIds);
    }

    /**
     * Resolves all provided stage names and checks whether the owner has at least one of them.
     *
     * @param ownerId the owner to query
     * @param stageNames the normalized stage names to check
     * @return {@code true} when the owner has any provided stage
     */
    public static boolean hasAnyStageSlow(UUID ownerId, String... stageNames) {
        return hasAnyStage(ownerId, resolveStageIds(stageNames));
    }

    /**
     * Checks whether the owner has at least one of the provided stage ids.
     *
     * @param ownerId the owner to query
     * @param stageIds the stage ids to check
     * @return {@code true} when the owner has any provided stage
     */
    public static boolean hasAnyStage(UUID ownerId, short... stageIds) {
        requireKnownStageIds(stageIds);
        return hasAnyStageFast(ownerId, stageIds);
    }

    /**
     * Checks whether the owner has at least one trusted stage id without validating them first.
     *
     * @param ownerId the owner to query
     * @param stageIds the trusted stage ids to check
     * @return {@code true} when the owner has any provided stage
     */
    public static boolean hasAnyStageFast(UUID ownerId, short... stageIds) {
        return PlayerStageDataService.hasAnyStage(ownerId, stageIds);
    }

    /**
     * Checks whether the owner has the provided trusted stage id without validating it first.
     *
     * @param ownerId the owner to query
     * @param stageId the trusted stage id to check
     * @return {@code true} when the owner has the stage
     */
    public static boolean hasStageFast(UUID ownerId, short stageId) {
        return PlayerStageDataService.hasStage(ownerId, stageId);
    }

    /**
     * Resolves a stage name and adds it to the owner.
     *
     * @param ownerId the target owner
     * @param stageName the normalized stage name
     * @return {@code true} when the owner gained the stage
     */
    public static boolean addStageSlow(UUID ownerId, String stageName) {
        return addStage(ownerId, getStageId(stageName));
    }

    /**
     * Adds a stage to the owner.
     *
     * @param ownerId the target owner
     * @param stageId the stage id to add
     * @return {@code true} when the owner gained the stage
     */
    public static boolean addStage(UUID ownerId, short stageId) {
        requireKnownStageId(stageId);
        return addStageFast(ownerId, stageId);
    }

    /**
     * Adds a trusted stage id to the owner without validating it first.
     *
     * @param ownerId the target owner
     * @param stageId the trusted stage id to add
     * @return {@code true} when the owner gained the stage
     */
    public static boolean addStageFast(UUID ownerId, short stageId) {
        return PlayerStageDataService.addStage(ownerId, stageId);
    }

    /**
     * Resolves a stage name and removes it from the owner.
     *
     * @param ownerId the target owner
     * @param stageName the normalized stage name
     * @return {@code true} when the owner lost the stage
     */
    public static boolean removeStageSlow(UUID ownerId, String stageName) {
        return removeStage(ownerId, getStageId(stageName));
    }

    /**
     * Removes a stage from the owner.
     *
     * @param ownerId the target owner
     * @param stageId the stage id to remove
     * @return {@code true} when the owner lost the stage
     */
    public static boolean removeStage(UUID ownerId, short stageId) {
        requireKnownStageId(stageId);
        return removeStageFast(ownerId, stageId);
    }

    /**
     * Removes a trusted stage id from the owner without validating it first.
     *
     * @param ownerId the target owner
     * @param stageId the trusted stage id to remove
     * @return {@code true} when the owner lost the stage
     */
    public static boolean removeStageFast(UUID ownerId, short stageId) {
        return PlayerStageDataService.removeStage(ownerId, stageId);
    }

    /**
     * Clears all stages from the owner.
     *
     * @param ownerId the target owner
     * @return {@code true} when any stage was removed
     */
    public static boolean clearStages(UUID ownerId) {
        return PlayerStageDataService.clearStages(ownerId);
    }

    /**
     * Replaces the owner's stages with the provided stage container.
     *
     * @param ownerId the target owner
     * @param stageData the new stage contents
     * @return {@code true} when the owner's stages changed
     */
    public static boolean setStages(UUID ownerId, StageData stageData) {
        return PlayerStageDataService.setStages(ownerId, stageData);
    }

    /**
     * Merges the provided stages into the owner.
     *
     * @param ownerId the target owner
     * @param stageData the stage contents to merge
     * @return {@code true} when the owner's stages changed
     */
    public static boolean mergeStages(UUID ownerId, StageData stageData) {
        return PlayerStageDataService.mergeStages(ownerId, stageData);
    }

    /**
     * Replaces the owner's stages with a raw bitset representation.
     *
     * @param ownerId the target owner
     * @param rawStages the raw stage bitset
     * @return {@code true} when the owner's stages changed
     */
    public static boolean replaceRawStages(UUID ownerId, long[] rawStages) {
        return PlayerStageDataService.replace(ownerId, rawStages);
    }

    /**
     * Copies the owner's stages into a detached snapshot object.
     *
     * @param ownerId the owner to snapshot
     * @return a detached copy of the owner's current stages
     */
    public static StageData snapshot(UUID ownerId) {
        return PlayerStageDataService.copy(ownerId);
    }

    public static boolean hasStageSlow(String stageName, ServerPlayer player) {
        return hasStageSlow(player.getUUID(), stageName);
    }

    public static boolean hasStage(short stageId, ServerPlayer player) {
        return hasStage(player.getUUID(), stageId);
    }

    public static boolean hasStagesSlow(ServerPlayer player, String... stageNames) {
        return hasStagesSlow(player.getUUID(), stageNames);
    }

    public static boolean hasStages(ServerPlayer player, short... stageIds) {
        return hasStages(player.getUUID(), stageIds);
    }

    public static boolean hasStagesFast(ServerPlayer player, short... stageIds) {
        return hasStagesFast(player.getUUID(), stageIds);
    }

    public static boolean hasAnyStageSlow(ServerPlayer player, String... stageNames) {
        return hasAnyStageSlow(player.getUUID(), stageNames);
    }

    public static boolean hasAnyStage(ServerPlayer player, short... stageIds) {
        return hasAnyStage(player.getUUID(), stageIds);
    }

    public static boolean hasAnyStageFast(ServerPlayer player, short... stageIds) {
        return hasAnyStageFast(player.getUUID(), stageIds);
    }

    public static boolean hasStageFast(short stageId, ServerPlayer player) {
        return hasStageFast(player.getUUID(), stageId);
    }

    public static boolean addStageSlow(String stageName, ServerPlayer player) {
        return addStageSlow(player.getUUID(), stageName);
    }

    public static boolean addStage(short stageId, ServerPlayer player) {
        return addStage(player.getUUID(), stageId);
    }

    public static boolean addStageFast(short stageId, ServerPlayer player) {
        return addStageFast(player.getUUID(), stageId);
    }

    public static boolean removeStageSlow(String stageName, ServerPlayer player) {
        return removeStageSlow(player.getUUID(), stageName);
    }

    public static boolean removeStage(short stageId, ServerPlayer player) {
        return removeStage(player.getUUID(), stageId);
    }

    public static boolean removeStageFast(short stageId, ServerPlayer player) {
        return removeStageFast(player.getUUID(), stageId);
    }

    public static boolean clearStages(ServerPlayer player) {
        return clearStages(player.getUUID());
    }

    public static boolean setStages(ServerPlayer player, StageData stageData) {
        return setStages(player.getUUID(), stageData);
    }

    public static boolean mergeStages(ServerPlayer player, StageData stageData) {
        return mergeStages(player.getUUID(), stageData);
    }

    public static boolean replaceRawStages(ServerPlayer player, long[] rawStages) {
        return replaceRawStages(player.getUUID(), rawStages);
    }

    public static StageData snapshot(ServerPlayer player) {
        return snapshot(player.getUUID());
    }

    private static short[] resolveStageIds(String[] stageNames) {
        short[] stageIds = new short[stageNames.length];
        for (int i = 0; i < stageNames.length; i++) {
            stageIds[i] = getStageId(stageNames[i]);
        }

        return stageIds;
    }

    private static void requireKnownStageIds(short[] stageIds) {
        for (short stageId : stageIds) {
            requireKnownStageId(stageId);
        }
    }

    private static void requireKnownStageId(short stageId) {
        if (!isKnownStageId(stageId)) {
            throw new IllegalArgumentException("Unknown stage id '" + stageId + "'");
        }
    }
}
