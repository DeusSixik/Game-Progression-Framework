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

    private final Object2ObjectOpenHashMap<UUID, long[]> playerStages = new Object2ObjectOpenHashMap<>();

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
            data.playerStages.put(ownerId, playerTag.getLongArray(STAGES_TAG));
        }

        return data;
    }

    public BaseBitStageData createStageData(UUID ownerId) {
        return new BaseBitStageData(copyRaw(ownerId), ownerId);
    }

    public long[] copyRaw(UUID ownerId) {
        long[] rawStages = playerStages.get(ownerId);
        return rawStages == null ? new long[0] : rawStages.clone();
    }

    public void writeRaw(UUID ownerId, long[] rawStages) {
        long[] copy = rawStages.clone();
        if (copy.length == 0) {
            playerStages.remove(ownerId);
        } else {
            playerStages.put(ownerId, copy);
        }

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
            playerTag.putLongArray(STAGES_TAG, playerStages.get(ownerId));
            players.add(playerTag);
        }

        tag.put(PLAYERS_TAG, players);
        return tag;
    }
}
