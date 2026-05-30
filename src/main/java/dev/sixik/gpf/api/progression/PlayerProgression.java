package dev.sixik.gpf.api.progression;

import dev.sixik.gpf.api.StageData;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Server-side progression view for a single player owner.
 */
public interface PlayerProgression {

    /**
     * @return the unique identifier of the represented owner
     */
    UUID getPlayerId();

    /**
     * @return the online player instance when available, or {@code null} when the owner is offline
     */
    @Nullable
    ServerPlayer getPlayer();

    /**
     * @return {@code true} when the represented owner is currently online
     */
    default boolean isPlayerOnline() {
        return getPlayer() != null;
    }

    /**
     * @return a detached snapshot of the owner's current stages
     */
    StageData snapshot();

    /**
     * Resolves a stage name and checks whether the owner has it.
     *
     * @param stageName the normalized stage name
     * @return {@code true} when the owner has the stage
     */
    boolean hasStage(String stageName);

    /**
     * Checks whether the owner has the provided stage id.
     *
     * @param stageId the stage id to check
     * @return {@code true} when the owner has the stage
     */
    boolean hasStage(short stageId);

    /**
     * Resolves all provided stage names and checks whether the owner has every one of them.
     *
     * @param stageNames the normalized stage names to check
     * @return {@code true} when the owner has all provided stages
     */
    boolean hasStages(String... stageNames);

    /**
     * Checks whether the owner has every provided stage id.
     *
     * @param stageIds the stage ids to check
     * @return {@code true} when the owner has all provided stages
     */
    boolean hasStages(short... stageIds);

    /**
     * Resolves the provided stage names and checks whether the owner has at least one of them.
     *
     * @param stageNames the normalized stage names to check
     * @return {@code true} when the owner has any provided stage
     */
    boolean hasAnyStage(String... stageNames);

    /**
     * Checks whether the owner has at least one of the provided stage ids.
     *
     * @param stageIds the stage ids to check
     * @return {@code true} when the owner has any provided stage
     */
    boolean hasAnyStage(short... stageIds);

    /**
     * Checks whether the owner has a trusted stage id without validating it first.
     *
     * @param stageId the trusted stage id to check
     * @return {@code true} when the owner has the stage
     */
    boolean hasStageFast(short stageId);

    /**
     * Checks whether the owner has every provided trusted stage id without validating them first.
     *
     * @param stageIds the trusted stage ids to check
     * @return {@code true} when the owner has all provided stages
     */
    boolean hasStagesFast(short... stageIds);

    /**
     * Checks whether the owner has at least one trusted stage id without validating them first.
     *
     * @param stageIds the trusted stage ids to check
     * @return {@code true} when the owner has any provided stage
     */
    boolean hasAnyStageFast(short... stageIds);

    /**
     * Resolves a stage name and adds it to the owner.
     *
     * @param stageName the normalized stage name
     * @return {@code true} when the owner gained the stage
     */
    boolean addStage(String stageName);

    /**
     * Adds a stage to the owner.
     *
     * @param stageId the stage id to add
     * @return {@code true} when the owner gained the stage
     */
    boolean addStage(short stageId);

    /**
     * Adds a trusted stage id to the owner without validating it first.
     *
     * @param stageId the trusted stage id to add
     * @return {@code true} when the owner gained the stage
     */
    boolean addStageFast(short stageId);

    /**
     * Resolves a stage name and removes it from the owner.
     *
     * @param stageName the normalized stage name
     * @return {@code true} when the owner lost the stage
     */
    boolean removeStage(String stageName);

    /**
     * Removes a stage from the owner.
     *
     * @param stageId the stage id to remove
     * @return {@code true} when the owner lost the stage
     */
    boolean removeStage(short stageId);

    /**
     * Removes a trusted stage id from the owner without validating it first.
     *
     * @param stageId the trusted stage id to remove
     * @return {@code true} when the owner lost the stage
     */
    boolean removeStageFast(short stageId);

    /**
     * Clears all stages from the owner.
     *
     * @return {@code true} when any stage was removed
     */
    boolean clearStages();

    /**
     * Replaces the owner's stages with the provided container.
     *
     * @param stageData the new stage contents
     * @return {@code true} when the owner's stages changed
     */
    boolean setStages(StageData stageData);

    /**
     * Merges the provided stages into the owner.
     *
     * @param stageData the stage contents to merge
     * @return {@code true} when the owner's stages changed
     */
    boolean mergeStages(StageData stageData);

    /**
     * Replaces the owner's stages with a raw bitset representation.
     *
     * @param rawStages the raw stage bitset
     * @return {@code true} when the owner's stages changed
     */
    boolean replaceRawStages(long[] rawStages);

    /**
     * Sends the current stage snapshot to the online client when present.
     */
    void sync();
}
