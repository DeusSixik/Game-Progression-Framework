package dev.sixik.gpf.impl.server;

import dev.sixik.gpf.api.StageData;
import dev.sixik.gpf.api.event.PlayerStageAddedEvent;
import dev.sixik.gpf.api.event.PlayerStageRemovedEvent;
import dev.sixik.gpf.api.event.PlayerStagesChangedEvent;
import dev.sixik.gpf.api.event.PlayerStagesClearedEvent;
import dev.sixik.gpf.impl.network.SendPlayerStagesToClientPacket;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.BitSet;

public final class PlayerStageDataService {

    private PlayerStageDataService() {
    }

    public static StageData getOrCreate(ServerPlayer player) {
        return getStorage(player.server).getOrCreate(player.getUUID());
    }

    public static boolean hasStage(ServerPlayer player, short stageId) {
        return getStorage(player.server).getOrCreate(player.getUUID()).hasStage(stageId);
    }

    public static boolean hasStages(ServerPlayer player, short... stageIds) {
        return getStorage(player.server).getOrCreate(player.getUUID()).hasStages(stageIds);
    }

    public static StageData copy(ServerPlayer player) {
        return getStorage(player.server).copyForSync(player.getUUID());
    }

    public static boolean addStage(ServerPlayer player, short stageId) {
        String stageName = getKnownStageName(stageId);
        boolean changed = getStorage(player.server).addStage(player.getUUID(), stageId);
        if (changed) {
            sync(player);
            NeoForge.EVENT_BUS.post(new PlayerStageAddedEvent(player, stageId, stageName));
        }

        return changed;
    }

    public static boolean removeStage(ServerPlayer player, short stageId) {
        String stageName = getKnownStageName(stageId);
        boolean changed = getStorage(player.server).removeStage(player.getUUID(), stageId);
        if (changed) {
            sync(player);
            NeoForge.EVENT_BUS.post(new PlayerStageRemovedEvent(player, stageId, stageName));
        }

        return changed;
    }

    public static boolean clearStages(ServerPlayer player) {
        StageData previousStages = copy(player);
        boolean changed = getStorage(player.server).clear(player.getUUID());
        if (changed) {
            sync(player);
            NeoForge.EVENT_BUS.post(new PlayerStagesClearedEvent(player, previousStages));
        }

        return changed;
    }

    public static boolean setStages(ServerPlayer player, StageData stageData) {
        StageData previousStages = copy(player);
        boolean changed = getStorage(player.server).set(player.getUUID(), stageData);
        if (changed) {
            postBatchChange(player, previousStages);
        }

        return changed;
    }

    public static boolean mergeStages(ServerPlayer player, StageData stageData) {
        StageData previousStages = copy(player);
        boolean changed = getStorage(player.server).merge(player.getUUID(), stageData);
        if (changed) {
            postBatchChange(player, previousStages);
        }

        return changed;
    }

    public static boolean replace(ServerPlayer player, long[] rawStages) {
        StageData previousStages = copy(player);
        boolean changed = getStorage(player.server).replaceIfChanged(player.getUUID(), rawStages);
        if (changed) {
            postBatchChange(player, previousStages);
        }

        return changed;
    }

    public static SendPlayerStagesToClientPacket createSyncPacket(ServerPlayer player) {
        StageData snapshot = copy(player);
        return new SendPlayerStagesToClientPacket(snapshot.getOwnerId(), snapshot.toRawDataCopy());
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, createSyncPacket(player));
    }

    private static PlayerStageDataSavedData getStorage(MinecraftServer server) {
        return PlayerStageDataSavedData.get(server);
    }

    private static String getKnownStageName(short stageId) {
        String stageName = StagesRegistry.INSTANCE.getName(stageId);
        if (stageName == null) {
            throw new IllegalArgumentException("Unknown stage id '" + stageId + "'");
        }

        return stageName;
    }

    private static void postBatchChange(ServerPlayer player, StageData previousStages) {
        StageData currentStages = copy(player);
        sync(player);
        NeoForge.EVENT_BUS.post(createChangedEvent(player, previousStages, currentStages));
    }

    private static PlayerStagesChangedEvent createChangedEvent(ServerPlayer player, StageData previousStages, StageData currentStages) {
        BitSet previousBits = BitSet.valueOf(previousStages.toRawData());
        BitSet currentBits = BitSet.valueOf(currentStages.toRawData());

        BitSet addedBits = (BitSet) currentBits.clone();
        addedBits.andNot(previousBits);

        BitSet removedBits = (BitSet) previousBits.clone();
        removedBits.andNot(currentBits);

        return new PlayerStagesChangedEvent(
                player,
                previousStages,
                currentStages,
                toStageIdArray(addedBits),
                toStageIdArray(removedBits)
        );
    }

    private static short[] toStageIdArray(BitSet bitSet) {
        ShortArrayList stageIds = new ShortArrayList(bitSet.cardinality());
        for (int stageId = bitSet.nextSetBit(0); stageId >= 0; stageId = bitSet.nextSetBit(stageId + 1)) {
            stageIds.add((short) stageId);
        }

        return stageIds.toShortArray();
    }
}
