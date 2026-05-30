package dev.sixik.gpf.impl.server;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import dev.sixik.gpf.api.StageData;
import dev.sixik.gpf.api.event.PlayerStageAddedEvent;
import dev.sixik.gpf.api.event.PlayerStageRemovedEvent;
import dev.sixik.gpf.api.event.PlayerStagesChangedEvent;
import dev.sixik.gpf.api.event.PlayerStagesClearedEvent;
import dev.sixik.gpf.data.BaseBitStageData;
import dev.sixik.gpf.impl.network.SendPlayerStagesToClientPacket;
import dev.sixik.gpf.registry.StagesRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.BitSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStageDataService {

    private static final Duration CACHE_EXPIRE_AFTER_ACCESS = Duration.ofMinutes(10);

    @Nullable
    private static RuntimeState runtimeState;

    private PlayerStageDataService() {
    }

    public static synchronized void initialize(MinecraftServer server) {
        shutdown();

        PlayerStageDataSavedData savedData = PlayerStageDataSavedData.get(server);
        Set<UUID> dirtyOwners = ConcurrentHashMap.newKeySet();
        Cache<UUID, BaseBitStageData> cache = Caffeine.newBuilder()
                .expireAfterAccess(CACHE_EXPIRE_AFTER_ACCESS)
                .removalListener((UUID ownerId, BaseBitStageData stageData, RemovalCause cause) -> {
                    if (ownerId == null || stageData == null || !dirtyOwners.remove(ownerId)) {
                        return;
                    }

                    savedData.writeRaw(ownerId, stageData.toRawDataCopy());
                })
                .build();

        runtimeState = new RuntimeState(server, savedData, cache, dirtyOwners);
    }

    public static synchronized void flushDirty() {
        RuntimeState state = runtime();
        ObjectArrayList<UUID> dirtySnapshot = new ObjectArrayList<>(state.dirtyOwners);
        for (UUID ownerId : dirtySnapshot) {
            BaseBitStageData stageData = state.cache.getIfPresent(ownerId);
            if (stageData != null) {
                state.savedData.writeRaw(ownerId, stageData.toRawDataCopy());
            }

            state.dirtyOwners.remove(ownerId);
        }
    }

    public static synchronized void shutdown() {
        RuntimeState state = runtimeState;
        if (state == null) {
            return;
        }

        flushDirty();
        state.cache.invalidateAll();
        runtimeState = null;
    }

    public static StageData getOrCreate(UUID ownerId) {
        return runtime().get(ownerId);
    }

    public static StageData getOrCreate(ServerPlayer player) {
        return getOrCreate(player.getUUID());
    }

    public static boolean hasStage(UUID ownerId, short stageId) {
        return runtime().get(ownerId).hasStage(stageId);
    }

    public static boolean hasStage(ServerPlayer player, short stageId) {
        return hasStage(player.getUUID(), stageId);
    }

    public static boolean hasStages(UUID ownerId, short... stageIds) {
        return runtime().get(ownerId).hasStages(stageIds);
    }

    public static boolean hasStages(ServerPlayer player, short... stageIds) {
        return hasStages(player.getUUID(), stageIds);
    }

    public static boolean hasAnyStage(UUID ownerId, short... stageIds) {
        return runtime().get(ownerId).hasAnyStage(stageIds);
    }

    public static boolean hasAnyStage(ServerPlayer player, short... stageIds) {
        return hasAnyStage(player.getUUID(), stageIds);
    }

    public static StageData copy(UUID ownerId) {
        BaseBitStageData stageData = runtime().get(ownerId);
        return new BaseBitStageData(stageData.toRawDataCopy(), ownerId);
    }

    public static StageData copy(ServerPlayer player) {
        return copy(player.getUUID());
    }

    public static boolean addStage(UUID ownerId, short stageId) {
        String stageName = getKnownStageName(stageId);
        BaseBitStageData stageData = runtime().get(ownerId);
        if (stageData.hasStage(stageId)) {
            return false;
        }

        stageData.addStage(stageId);
        markDirty(ownerId);
        sync(ownerId);
        NeoForge.EVENT_BUS.post(new PlayerStageAddedEvent(ownerId, getOnlinePlayer(ownerId), stageId, stageName));
        return true;
    }

    public static boolean addStage(ServerPlayer player, short stageId) {
        return addStage(player.getUUID(), stageId);
    }

    public static boolean removeStage(UUID ownerId, short stageId) {
        String stageName = getKnownStageName(stageId);
        BaseBitStageData stageData = runtime().get(ownerId);
        boolean changed = stageData.removeStage(stageId);
        if (!changed) {
            return false;
        }

        markDirty(ownerId);
        sync(ownerId);
        NeoForge.EVENT_BUS.post(new PlayerStageRemovedEvent(ownerId, getOnlinePlayer(ownerId), stageId, stageName));
        return true;
    }

    public static boolean removeStage(ServerPlayer player, short stageId) {
        return removeStage(player.getUUID(), stageId);
    }

    public static boolean clearStages(UUID ownerId) {
        StageData previousStages = copy(ownerId);
        BaseBitStageData stageData = runtime().get(ownerId);
        if (stageData.toRawData().length == 0) {
            return false;
        }

        stageData.clearAllStages();
        markDirty(ownerId);
        sync(ownerId);
        NeoForge.EVENT_BUS.post(new PlayerStagesClearedEvent(ownerId, getOnlinePlayer(ownerId), previousStages));
        return true;
    }

    public static boolean clearStages(ServerPlayer player) {
        return clearStages(player.getUUID());
    }

    public static boolean setStages(UUID ownerId, StageData other) {
        StageData previousStages = copy(ownerId);
        BaseBitStageData current = runtime().get(ownerId);
        long[] previousRaw = current.toRawDataCopy();
        current.set(other);
        if (BitSet.valueOf(previousRaw).equals(BitSet.valueOf(current.toRawData()))) {
            return false;
        }

        markDirty(ownerId);
        postBatchChange(ownerId, previousStages);
        return true;
    }

    public static boolean setStages(ServerPlayer player, StageData other) {
        return setStages(player.getUUID(), other);
    }

    public static boolean mergeStages(UUID ownerId, StageData other) {
        StageData previousStages = copy(ownerId);
        BaseBitStageData current = runtime().get(ownerId);
        long[] previousRaw = current.toRawDataCopy();
        current.merge(other);
        if (BitSet.valueOf(previousRaw).equals(BitSet.valueOf(current.toRawData()))) {
            return false;
        }

        markDirty(ownerId);
        postBatchChange(ownerId, previousStages);
        return true;
    }

    public static boolean mergeStages(ServerPlayer player, StageData other) {
        return mergeStages(player.getUUID(), other);
    }

    public static boolean replace(UUID ownerId, long[] rawStages) {
        StageData previousStages = copy(ownerId);
        BaseBitStageData current = runtime().get(ownerId);
        long[] previousRaw = current.toRawDataCopy();
        BitSet replacementBits = BitSet.valueOf(rawStages);
        if (BitSet.valueOf(previousRaw).equals(replacementBits)) {
            return false;
        }

        current.set(new BaseBitStageData(rawStages, ownerId));
        markDirty(ownerId);
        postBatchChange(ownerId, previousStages);
        return true;
    }

    public static boolean replace(ServerPlayer player, long[] rawStages) {
        return replace(player.getUUID(), rawStages);
    }

    public static SendPlayerStagesToClientPacket createSyncPacket(UUID ownerId) {
        StageData snapshot = copy(ownerId);
        return new SendPlayerStagesToClientPacket(snapshot.getOwnerId(), snapshot.toRawDataCopy());
    }

    public static SendPlayerStagesToClientPacket createSyncPacket(ServerPlayer player) {
        return createSyncPacket(player.getUUID());
    }

    public static void sync(UUID ownerId) {
        ServerPlayer player = getOnlinePlayer(ownerId);
        if (player != null) {
            PacketDistributor.sendToPlayer(player, createSyncPacket(ownerId));
        }
    }

    public static void sync(ServerPlayer player) {
        sync(player.getUUID());
    }

    @Nullable
    public static ServerPlayer getOnlinePlayer(UUID ownerId) {
        RuntimeState state = runtimeState;
        if (state == null) {
            return null;
        }

        return state.server.getPlayerList().getPlayer(ownerId);
    }

    @Nullable
    public static MinecraftServer getServer() {
        RuntimeState state = runtimeState;
        return state == null ? null : state.server;
    }

    private static RuntimeState runtime() {
        RuntimeState state = runtimeState;
        if (state == null) {
            throw new IllegalStateException("Player stage data service is not initialized");
        }

        return state;
    }

    private static void markDirty(UUID ownerId) {
        runtime().dirtyOwners.add(ownerId);
    }

    private static String getKnownStageName(short stageId) {
        String stageName = StagesRegistry.INSTANCE.getName(stageId);
        if (stageName == null) {
            throw new IllegalArgumentException("Unknown stage id '" + stageId + "'");
        }

        return stageName;
    }

    private static void postBatchChange(UUID ownerId, StageData previousStages) {
        StageData currentStages = copy(ownerId);
        sync(ownerId);
        NeoForge.EVENT_BUS.post(createChangedEvent(ownerId, previousStages, currentStages));
    }

    private static PlayerStagesChangedEvent createChangedEvent(UUID ownerId, StageData previousStages, StageData currentStages) {
        BitSet previousBits = BitSet.valueOf(previousStages.toRawData());
        BitSet currentBits = BitSet.valueOf(currentStages.toRawData());

        BitSet addedBits = (BitSet) currentBits.clone();
        addedBits.andNot(previousBits);

        BitSet removedBits = (BitSet) previousBits.clone();
        removedBits.andNot(currentBits);

        return new PlayerStagesChangedEvent(
                ownerId,
                getOnlinePlayer(ownerId),
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

    private record RuntimeState(
            MinecraftServer server,
            PlayerStageDataSavedData savedData,
            Cache<UUID, BaseBitStageData> cache,
            Set<UUID> dirtyOwners
    ) {
        private BaseBitStageData get(UUID ownerId) {
            return cache.get(ownerId, savedData::createStageData);
        }
    }
}
