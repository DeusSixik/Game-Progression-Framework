package dev.sixik.gpf.impl.server;

import dev.sixik.gpf.GameProgressionFramework;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Comparator;
import java.util.List;

public final class StageRegistrySavedData extends SavedData {

    private static final String DATA_NAME = GameProgressionFramework.MODID + "_stage_registry";
    private static final String ENTRIES_TAG = "entries";
    private static final String ID_TAG = "id";
    private static final String NAME_TAG = "name";

    private final Object2ShortOpenHashMap<String> stageIds = new Object2ShortOpenHashMap<>();
    private final Short2ObjectOpenHashMap<String> idToStage = new Short2ObjectOpenHashMap<>();
    private short nextStageId;

    public StageRegistrySavedData() {
        stageIds.defaultReturnValue((short) -1);
    }

    public static StageRegistrySavedData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(StageRegistrySavedData::new, StageRegistrySavedData::load), DATA_NAME);
    }

    private static StageRegistrySavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        StageRegistrySavedData data = new StageRegistrySavedData();
        ListTag entries = tag.getList(ENTRIES_TAG, Tag.TAG_COMPOUND);

        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            data.registerLoadedStage(entry.getString(NAME_TAG), entry.getShort(ID_TAG));
        }

        return data;
    }

    private void registerLoadedStage(String stageName, short stageId) {
        short existingId = stageIds.getShort(stageName);
        if (existingId != -1 && existingId != stageId) {
            throw new IllegalStateException("Stage '" + stageName + "' is stored with multiple ids");
        }

        String existingName = idToStage.get(stageId);
        if (existingName != null && !existingName.equals(stageName)) {
            throw new IllegalStateException("Stage id " + stageId + " is stored for both '" + existingName + "' and '" + stageName + "'");
        }

        stageIds.put(stageName, stageId);
        idToStage.put(stageId, stageName);
        nextStageId = (short) Math.max(nextStageId, stageId + 1);
    }

    public List<Object2ShortMap.Entry<String>> getEntriesSortedById() {
        ObjectArrayList<Object2ShortMap.Entry<String>> entries = new ObjectArrayList<>(stageIds.object2ShortEntrySet());
        entries.sort(Comparator.comparingInt(Object2ShortMap.Entry::getShortValue));
        return entries;
    }

    public short getNextStageId() {
        return nextStageId;
    }

    public boolean containsStage(String stageName) {
        return stageIds.containsKey(stageName);
    }

    public short getStageId(String stageName) {
        return stageIds.getShort(stageName);
    }

    public String getStageName(short stageId) {
        return idToStage.get(stageId);
    }

    public void addStage(String stageName, short stageId) {
        registerLoadedStage(stageName, stageId);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag entries = new ListTag();

        for (Object2ShortMap.Entry<String> entry : getEntriesSortedById()) {
            CompoundTag stageTag = new CompoundTag();
            stageTag.putString(NAME_TAG, entry.getKey());
            stageTag.putShort(ID_TAG, entry.getShortValue());
            entries.add(stageTag);
        }

        tag.put(ENTRIES_TAG, entries);
        return tag;
    }
}
