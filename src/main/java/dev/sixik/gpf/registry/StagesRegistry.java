package dev.sixik.gpf.registry;

import dev.sixik.gpf.GameProgressionFramework;
import dev.sixik.gpf.api.event.StageRegisterEndEvent;
import dev.sixik.gpf.api.event.StageRegisterEvent;
import dev.sixik.gpf.api.script.StageScriptApi;
import dev.sixik.gpf.impl.network.SendStagesToClientPacket;
import dev.sixik.gpf.impl.server.StageRegistrySavedData;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class StagesRegistry {

    public static final short UNKNOWN_STAGE = -1;
    private static final Pattern VALID_STAGE_NAME = Pattern.compile("^[a-z0-9_./-]+(?::[a-z0-9_./-]+)?$");
    private static final RegistrySnapshot EMPTY_SNAPSHOT = RegistrySnapshot.empty();

    public static StagesRegistry INSTANCE;

    private short currentStageId;
    private final Object2ShortOpenHashMap<String> stageIds = new Object2ShortOpenHashMap<>();
    private final Short2ObjectOpenHashMap<String> idToStage = new Short2ObjectOpenHashMap<>();
    private final ObjectOpenHashSet<String> pendingStages = new ObjectOpenHashSet<>();
    private final ObjectOpenHashSet<String> activeStages = new ObjectOpenHashSet<>();
    private volatile RegistrySnapshot snapshot = EMPTY_SNAPSHOT;
    private boolean collectingStages;
    private boolean pendingFinalization;

    public StagesRegistry() {
        stageIds.defaultReturnValue(UNKNOWN_STAGE);
    }

    public synchronized void reloadFromScripts() {
        pendingStages.clear();
        collectingStages = true;
        try {
            NeoForge.EVENT_BUS.post(new StageRegisterEvent(new StageScriptApi(this)));
        } finally {
            collectingStages = false;
        }

        pendingFinalization = true;
        tryFinalize(ServerLifecycleHooks.getCurrentServer());
    }

    public synchronized void finalizePendingStages(MinecraftServer server) {
        tryFinalize(Objects.requireNonNull(server, "Server cannot be null"));
    }

    private void tryFinalize(MinecraftServer server) {
        if (!pendingFinalization || server == null || server.overworld() == null) {
            return;
        }

        StageRegistrySavedData savedData = StageRegistrySavedData.get(server);
        loadStoredStages(savedData);

        ObjectArrayList<String> newStages = new ObjectArrayList<>();
        for (String stageName : pendingStages) {
            if (!stageIds.containsKey(stageName)) {
                newStages.add(stageName);
            }
        }

        newStages.sort(Comparator.naturalOrder());
        for (String stageName : newStages) {
            short newId = nextStageId();
            stageIds.put(stageName, newId);
            idToStage.put(newId, stageName);
            savedData.addStage(stageName, newId);

            GameProgressionFramework.LOGGER.info("Registered new stage '{}'", stageName);
        }

        rebuildActiveStages();
        rebuildSnapshot();
        pendingFinalization = false;

        NeoForge.EVENT_BUS.post(new StageRegisterEndEvent(new StageScriptApi(this)));
    }

    public synchronized void clearRuntimeState() {
        clearRegistryMaps();
        pendingStages.clear();
        activeStages.clear();
        collectingStages = false;
        pendingFinalization = false;
        snapshot = EMPTY_SNAPSHOT;
    }

    public synchronized void registerStage(String stageName) {
        if (!collectingStages) {
            throw new IllegalStateException("Stages can only be registered during StageRegisterEvent");
        }

        pendingStages.add(validateStageName(stageName));
    }

    public SendStagesToClientPacket createSyncPacket() {
        return snapshot.createSyncPacket();
    }

    public synchronized void applySyncedStages(List<SendStagesToClientPacket.StageEntry> syncedStages, short[] syncedActiveStageIds) {
        clearRegistryMaps();
        pendingStages.clear();
        activeStages.clear();
        collectingStages = false;
        pendingFinalization = false;

        for (SendStagesToClientPacket.StageEntry stage : syncedStages) {
            putStage(stage.stageName(), stage.stageId());
        }

        for (short activeStageId : syncedActiveStageIds) {
            String activeStageName = idToStage.get(activeStageId);
            if (activeStageName != null) {
                activeStages.add(activeStageName);
            }
        }

        rebuildSnapshot();
    }

    private void loadStoredStages(StageRegistrySavedData savedData) {
        clearRegistryMaps();

        for (Object2ShortMap.Entry<String> entry : savedData.getEntriesSortedById()) {
            putStage(entry.getKey(), entry.getShortValue());
        }
    }

    private void rebuildActiveStages() {
        activeStages.clear();
        activeStages.addAll(pendingStages);
    }

    private void clearRegistryMaps() {
        currentStageId = 0;
        stageIds.clear();
        idToStage.clear();
    }

    private void rebuildSnapshot() {
        Object2ShortOpenHashMap<String> snapshotStageIds = new Object2ShortOpenHashMap<>(stageIds);
        snapshotStageIds.defaultReturnValue(UNKNOWN_STAGE);

        String[] snapshotIdToStage = new String[currentStageId];
        ObjectArrayList<SendStagesToClientPacket.StageEntry> syncedStages = new ObjectArrayList<>(stageIds.size());
        ShortArrayList syncedActiveStageIds = new ShortArrayList(activeStages.size());

        for (Object2ShortMap.Entry<String> entry : getStageEntriesSortedById()) {
            short stageId = entry.getShortValue();
            String stageName = entry.getKey();

            if (stageId >= 0 && stageId < snapshotIdToStage.length) {
                snapshotIdToStage[stageId] = stageName;
            }

            syncedStages.add(new SendStagesToClientPacket.StageEntry(stageId, stageName));
            if (activeStages.contains(stageName)) {
                syncedActiveStageIds.add(stageId);
            }
        }

        ObjectOpenHashSet<String> snapshotActiveStages = new ObjectOpenHashSet<>(activeStages);
        ObjectArrayList<String> sortedActiveStages = new ObjectArrayList<>(activeStages);
        sortedActiveStages.sort(Comparator.naturalOrder());

        snapshot = new RegistrySnapshot(
                snapshotStageIds,
                snapshotIdToStage,
                snapshotActiveStages,
                List.copyOf(syncedStages),
                syncedActiveStageIds.toShortArray(),
                List.copyOf(sortedActiveStages),
                currentStageId
        );
    }

    private void putStage(String stageName, short stageId) {
        short existingId = stageIds.getShort(stageName);
        if (existingId != UNKNOWN_STAGE && existingId != stageId) {
            throw new IllegalStateException("Stage '" + stageName + "' is already bound to id " + existingId);
        }

        String existingName = idToStage.get(stageId);
        if (existingName != null && !existingName.equals(stageName)) {
            throw new IllegalStateException("Stage id " + stageId + " is already bound to '" + existingName + "'");
        }

        stageIds.put(stageName, stageId);
        idToStage.put(stageId, stageName);
        currentStageId = (short) Math.max(currentStageId, stageId + 1);
    }

    private List<Object2ShortMap.Entry<String>> getStageEntriesSortedById() {
        ObjectArrayList<Object2ShortMap.Entry<String>> entries = new ObjectArrayList<>(stageIds.object2ShortEntrySet());
        entries.sort(Comparator.comparingInt(Object2ShortMap.Entry::getShortValue));
        return entries;
    }

    private short nextStageId() {
        if (currentStageId == Short.MAX_VALUE) {
            throw new IllegalStateException("Stage id space is exhausted");
        }

        return currentStageId++;
    }

    private String validateStageName(String stageName) {
        String normalized = Objects.requireNonNull(stageName, "Stage name cannot be null").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Stage name cannot be blank");
        }

        if (!VALID_STAGE_NAME.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid stage name '" + normalized + "'. Use lowercase letters, digits, '_', '-', '.', '/', and an optional namespace");
        }

        return normalized;
    }

    public short getId(String stageName) {
        return snapshot.getId(stageName);
    }

    public short getIdFast(String normalizedStageName) {
        return snapshot.getId(normalizedStageName);
    }

    public short getIdOrThrow(String stageName) {
        String normalized = validateStageName(stageName);
        short stageId = snapshot.getId(normalized);
        if (stageId == UNKNOWN_STAGE) {
            throw new IllegalArgumentException("Unknown stage '" + normalized + "'");
        }

        return stageId;
    }

    public boolean isKnownStage(String stageName) {
        return snapshot.isKnownStage(validateStageName(stageName));
    }

    public boolean isKnownStageFast(String normalizedStageName) {
        return snapshot.isKnownStage(normalizedStageName);
    }

    public boolean isActiveStage(String stageName) {
        return snapshot.isActiveStage(validateStageName(stageName));
    }

    public boolean isActiveStageFast(String normalizedStageName) {
        return snapshot.isActiveStage(normalizedStageName);
    }

    public boolean isKnownStageId(short id) {
        return snapshot.getName(id) != null;
    }

    public String getName(short id) {
        return snapshot.getName(id);
    }

    public int getRegisteredCount() {
        return snapshot.getRegisteredCount();
    }

    public List<String> getActiveStages() {
        return snapshot.getActiveStages();
    }

    private static final class RegistrySnapshot {

        private final Object2ShortOpenHashMap<String> stageIds;
        private final String[] idToStage;
        private final ObjectOpenHashSet<String> activeStages;
        private final List<SendStagesToClientPacket.StageEntry> syncedStages;
        private final short[] syncedActiveStageIds;
        private final List<String> sortedActiveStages;
        private final int registeredCount;

        private RegistrySnapshot(
                Object2ShortOpenHashMap<String> stageIds,
                String[] idToStage,
                ObjectOpenHashSet<String> activeStages,
                List<SendStagesToClientPacket.StageEntry> syncedStages,
                short[] syncedActiveStageIds,
                List<String> sortedActiveStages,
                int registeredCount
        ) {
            this.stageIds = stageIds;
            this.idToStage = idToStage;
            this.activeStages = activeStages;
            this.syncedStages = syncedStages;
            this.syncedActiveStageIds = syncedActiveStageIds;
            this.sortedActiveStages = sortedActiveStages;
            this.registeredCount = registeredCount;
        }

        private static RegistrySnapshot empty() {
            Object2ShortOpenHashMap<String> stageIds = new Object2ShortOpenHashMap<>();
            stageIds.defaultReturnValue(UNKNOWN_STAGE);

            return new RegistrySnapshot(
                    stageIds,
                    new String[0],
                    new ObjectOpenHashSet<>(),
                    List.of(),
                    new short[0],
                    List.of(),
                    0
            );
        }

        private short getId(String stageName) {
            return stageIds.getShort(stageName);
        }

        private boolean isKnownStage(String stageName) {
            return stageIds.getShort(stageName) != UNKNOWN_STAGE;
        }

        private boolean isActiveStage(String stageName) {
            return activeStages.contains(stageName);
        }

        private String getName(short id) {
            return id >= 0 && id < idToStage.length ? idToStage[id] : null;
        }

        private int getRegisteredCount() {
            return registeredCount;
        }

        private List<String> getActiveStages() {
            return sortedActiveStages;
        }

        private SendStagesToClientPacket createSyncPacket() {
            return new SendStagesToClientPacket(syncedStages, syncedActiveStageIds.clone());
        }
    }
}
