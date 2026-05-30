package dev.sixik.gpf.api.progression;

import dev.sixik.gpf.api.StageData;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Server-side progression view for a single player.
 */
public interface PlayerProgression {

    /**
     * @return the player represented by this progression view
     */
    ServerPlayer getPlayer();

    /**
     * @return the unique identifier of the represented player
     */
    default UUID getPlayerId() {
        return getPlayer().getUUID();
    }

    /**
     * @return a detached snapshot of the player's current stages
     */
    StageData snapshot();

    /**
     * Resolves a stage name and checks whether the player has it.
     *
     * @param stageName the normalized stage name
     * @return {@code true} when the player has the stage
     */
    boolean hasStage(String stageName);

    /**
     * Checks whether the player has the provided stage id.
     *
     * @param stageId the stage id to check
     * @return {@code true} when the player has the stage
     */
    boolean hasStage(short stageId);

    /**
     * Resolves all provided stage names and checks whether the player has every one of them.
     *
     * @param stageNames the normalized stage names to check
     * @return {@code true} when the player has all provided stages
     */
    boolean hasStages(String... stageNames);

    /**
     * Checks whether the player has every provided stage id.
     *
     * @param stageIds the stage ids to check
     * @return {@code true} when the player has all provided stages
     */
    boolean hasStages(short... stageIds);

    /**
     * Checks whether the player has a trusted stage id without validating it first.
     *
     * @param stageId the trusted stage id to check
     * @return {@code true} when the player has the stage
     */
    boolean hasStageFast(short stageId);

    /**
     * Checks whether the player has every provided trusted stage id without validating them first.
     *
     * @param stageIds the trusted stage ids to check
     * @return {@code true} when the player has all provided stages
     */
    boolean hasStagesFast(short... stageIds);

    /**
     * Resolves a stage name and adds it to the player.
     *
     * @param stageName the normalized stage name
     * @return {@code true} when the player gained the stage
     */
    boolean addStage(String stageName);

    /**
     * Adds a stage to the player.
     *
     * @param stageId the stage id to add
     * @return {@code true} when the player gained the stage
     */
    boolean addStage(short stageId);

    /**
     * Adds a trusted stage id to the player without validating it first.
     *
     * @param stageId the trusted stage id to add
     * @return {@code true} when the player gained the stage
     */
    boolean addStageFast(short stageId);

    /**
     * Resolves a stage name and removes it from the player.
     *
     * @param stageName the normalized stage name
     * @return {@code true} when the player lost the stage
     */
    boolean removeStage(String stageName);

    /**
     * Removes a stage from the player.
     *
     * @param stageId the stage id to remove
     * @return {@code true} when the player lost the stage
     */
    boolean removeStage(short stageId);

    /**
     * Removes a trusted stage id from the player without validating it first.
     *
     * @param stageId the trusted stage id to remove
     * @return {@code true} when the player lost the stage
     */
    boolean removeStageFast(short stageId);

    /**
     * Clears all stages from the player.
     *
     * @return {@code true} when any stage was removed
     */
    boolean clearStages();

    /**
     * Replaces the player's stages with the provided container.
     *
     * @param stageData the new stage contents
     * @return {@code true} when the player's stages changed
     */
    boolean setStages(StageData stageData);

    /**
     * Merges the provided stages into the player.
     *
     * @param stageData the stage contents to merge
     * @return {@code true} when the player's stages changed
     */
    boolean mergeStages(StageData stageData);

    /**
     * Replaces the player's stages with a raw bitset representation.
     *
     * @param rawStages the raw stage bitset
     * @return {@code true} when the player's stages changed
     */
    boolean replaceRawStages(long[] rawStages);

    /**
     * Sends the current stage snapshot to the client.
     */
    void sync();
}
