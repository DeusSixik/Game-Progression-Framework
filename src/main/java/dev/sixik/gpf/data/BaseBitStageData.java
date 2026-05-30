package dev.sixik.gpf.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.sixik.gpf.api.StageData;

import java.util.BitSet;
import java.util.List;
import java.util.UUID;

public class BaseBitStageData implements StageData {

    public static final Codec<BaseBitStageData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.listOf().fieldOf("stages").forGetter(BaseBitStageData::toLongList),
                    Codec.STRING.fieldOf("owner_id").forGetter(BaseBitStageData::getOwnerString)
            ).apply(instance, BaseBitStageData::new)
    );

    protected final BitSet unlockedStages;
    protected final UUID ownerId;

    public BaseBitStageData(List<Long> stages, String ownerId) {
        this.ownerId = UUID.fromString(ownerId);
        long[] rawArray = stages.stream().mapToLong(Long::longValue).toArray();
        this.unlockedStages = BitSet.valueOf(rawArray);
    }

    public BaseBitStageData(long[] stages, String ownerId) {
        this.ownerId = UUID.fromString(ownerId);
        this.unlockedStages = BitSet.valueOf(stages);
    }

    public BaseBitStageData(long[] stages, UUID ownerId) {
        this.ownerId = ownerId;
        this.unlockedStages = BitSet.valueOf(stages);
    }

    public BaseBitStageData(UUID ownerId) {
        this.unlockedStages = new BitSet();
        this.ownerId = ownerId;
    }

    @Override
    public UUID getOwnerId() {
        return ownerId;
    }

    @Override
    public void addStage(short stageId) {
        unlockedStages.set(stageId);
    }

    @Override
    public void removeStageFast(short stageId) {
        unlockedStages.clear(stageId);
    }

    @Override
    public boolean removeStage(short stageId) {
        boolean hadStage = unlockedStages.get(stageId);

        if (hadStage)
            unlockedStages.clear(stageId);

        return hadStage;
    }

    @Override
    public boolean hasStage(short stageId) {
        return unlockedStages.get(stageId);
    }

    @Override
    public boolean hasStages(short... stageIds) {
        for (int i = 0; i < stageIds.length; i++) {
            if (!unlockedStages.get(stageIds[i])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean hasAnyStage(short... stageIds) {
        for (int i = 0; i < stageIds.length; i++) {
            if (unlockedStages.get(stageIds[i])) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void clearAllStages() {
        unlockedStages.clear();
    }

    @Override
    public long[] toRawData() {
        return unlockedStages.toLongArray();
    }

    @Override
    public void set(StageData other) {
        unlockedStages.clear();
        if(other instanceof BaseBitStageData baseStageData) {
            unlockedStages.or((baseStageData.unlockedStages));
        } else {
            unlockedStages.or(BitSet.valueOf(other.toRawData()));
        }
    }

    @Override
    public void merge(StageData other) {
        if(other instanceof BaseBitStageData baseStageData) {
            unlockedStages.or((baseStageData.unlockedStages));
        } else {
            unlockedStages.or(BitSet.valueOf(other.toRawData()));
        }
    }
}
