package dev.sixik.gpf.api;

import dev.sixik.gpf.registry.StagesRegistry;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.UUID;

/**
 * Mutable stage container for a single owner.
 */
public interface StageData {

    /**
     * @return the unique identifier of the owner of this stage container
     */
    UUID getOwnerId();

    /**
     * Resolves the provided stage name and adds it to this container.
     *
     * @param stage the normalized stage name
     */
    default void addStageSlow(String stage) {
        addStage(StagesRegistry.INSTANCE.getIdOrThrow(stage));
    }

    /**
     * Adds the provided stage id to this container.
     *
     * @param stageId the stage id to add
     */
    void addStage(short stageId);

    /**
     * Resolves the provided stage name and removes it from this container.
     *
     * @param stage the normalized stage name
     * @return {@code true} when the stage was present and removed
     */
    default boolean removeStageSlow(String stage) {
        return removeStage(StagesRegistry.INSTANCE.getIdOrThrow(stage));
    }

    /**
     * Removes the provided stage id without reporting whether it was present.
     *
     * @param stageId the stage id to remove
     */
    void removeStageFast(short stageId);

    /**
     * Removes the provided stage id from this container.
     *
     * @param stageId the stage id to remove
     * @return {@code true} when the stage was present and removed
     */
    boolean removeStage(short stageId);

    /**
     * Resolves the provided stage name and checks whether it is present.
     *
     * @param stage the normalized stage name
     * @return {@code true} when the stage is present
     */
    default boolean hasStageSlow(String stage) {
        return hasStage(StagesRegistry.INSTANCE.getIdOrThrow(stage));
    }

    /**
     * Checks whether the provided stage id is present.
     *
     * @param stageId the stage id to check
     * @return {@code true} when the stage is present
     */
    boolean hasStage(short stageId);

    /**
     * Checks whether all provided stage ids are present.
     *
     * @param stageIds the stage ids to check
     * @return {@code true} when all stages are present
     */
    default boolean hasStages(short... stageIds) {
        for (short stageId : stageIds) {
            if (!hasStage(stageId)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether at least one of the provided stage ids is present.
     *
     * @param stageIds the stage ids to check
     * @return {@code true} when any stage is present
     */
    default boolean hasAnyStage(short... stageIds) {
        for (short stageId : stageIds) {
            if (hasStage(stageId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Merges all stages from another container into this one.
     *
     * @param other the source container
     */
    void merge(StageData other);

    /**
     * Replaces the current contents with the contents of another container.
     *
     * @param other the source container
     */
    void set(StageData other);

    /**
     * Removes all stages from this container.
     */
    void clearAllStages();

    /**
     * @return the raw bitset representation of this container
     */
    long[] toRawData();

    /**
     * @return a defensive copy of the raw bitset representation
     */
    default long[] toRawDataCopy() {
        return toRawData().clone();
    }

    /**
     * @return the raw bitset representation as a list of longs
     */
    default LongList toLongList() {
        return new LongArrayList(toRawData());
    }

    /**
     * @return the owner id formatted as a string
     */
    default String getOwnerString() {
        return getOwnerId().toString();
    }
}
