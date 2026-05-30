package dev.sixik.gpf.impl.server;

import dev.sixik.gpf.GameProgressionFramework;
import dev.sixik.gpf.data.BaseBitStageData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class PlayerStageDataSavedData extends SavedData {

    private static final String DATA_NAME = GameProgressionFramework.MODID + "_player_stages";
    private static final String PLAYERS_TAG = "players";
    private static final String OWNER_TAG = "owner";
    private static final String STAGES_TAG = "stages";

    private final Object2ObjectOpenHashMap<UUID, BaseBitStageData> playerStages = new Object2ObjectOpenHashMap<>();

    public static PlayerStageDataSavedData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(PlayerStageDataSavedData::new, PlayerStageDataSavedData::load), DATA_NAME);
    }

    private static PlayerStageDataSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        PlayerStageDataSavedData data = new PlayerStageDataSavedData();
        ListTag players = tag.getList(PLAYERS_TAG, Tag.TAG_COMPOUND);

        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);
            UUID ownerId = playerTag.getUUID(OWNER_TAG);
            data.playerStages.put(ownerId, new BaseBitStageData(playerTag.getLongArray(STAGES_TAG), ownerId));
        }

        return data;
    }

    public BaseBitStageData getOrCreate(UUID ownerId) {
        BaseBitStageData stageData = playerStages.get(ownerId);
        if (stageData == null) {
            stageData = new BaseBitStageData(ownerId);
            playerStages.put(ownerId, stageData);
        }

        return stageData;
    }

    public BaseBitStageData copyForSync(UUID ownerId) {
        BaseBitStageData stageData = getOrCreate(ownerId);
        return new BaseBitStageData(stageData.toRawDataCopy(), ownerId);
    }

    public boolean addStage(UUID ownerId, short stageId) {
        BaseBitStageData stageData = getOrCreate(ownerId);
        if (stageData.hasStage(stageId)) {
            return false;
        }

        stageData.addStage(stageId);
        setDirty();
        return true;
    }

    public boolean removeStage(UUID ownerId, short stageId) {
        BaseBitStageData stageData = getOrCreate(ownerId);
        boolean removed = stageData.removeStage(stageId);
        if (removed) {
            setDirty();
        }

        return removed;
    }

    public void replace(UUID ownerId, long[] rawStages) {
        playerStages.put(ownerId, new BaseBitStageData(rawStages, ownerId));
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag players = new ListTag();
        List<UUID> ownerIds = new ObjectArrayList<>(playerStages.keySet());
        ownerIds.sort(Comparator.comparing(UUID::toString));

        for (UUID ownerId : ownerIds) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID(OWNER_TAG, ownerId);
            playerTag.putLongArray(STAGES_TAG, playerStages.get(ownerId).toRawDataCopy());
            players.add(playerTag);
        }

        tag.put(PLAYERS_TAG, players);
        return tag;
    }
}
