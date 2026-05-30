package dev.sixik.gpf.api;

import dev.sixik.gpf.registry.StagesRegistry;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.UUID;

public interface StageData {

    UUID getOwnerId();

    default void addStageSlow(String stage) {
        addStage(StagesRegistry.INSTANCE.getIdOrThrow(stage));
    }

    void addStage(short stageId);

    default boolean removeStageSlow(String stage) {
        return removeStage(StagesRegistry.INSTANCE.getIdOrThrow(stage));
    }

    void removeStageFast(short stageId);

    boolean removeStage(short stageId);

    default boolean hasStageSlow(String stage) {
        return hasStage(StagesRegistry.INSTANCE.getIdOrThrow(stage));
    }

    boolean hasStage(short stageId);

    void merge(StageData other);

    void set(StageData other);

    void clearAllStages();

    long[] toRawData();

    default long[] toRawDataCopy() {
        return toRawData().clone();
    }

    default LongList toLongList() {
        return new LongArrayList(toRawData());
    }

    default String getOwnerString() {
        return getOwnerId().toString();
    }
}
