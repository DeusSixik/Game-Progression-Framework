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

    public static StagesRegistry INSTANCE;

    private short currentStageId;
    private final Object2ShortOpenHashMap<String> stageIds = new Object2ShortOpenHashMap<>();
    private final Short2ObjectOpenHashMap<String> idToStage = new Short2ObjectOpenHashMap<>();
    private final ObjectOpenHashSet<String> pendingStages = new ObjectOpenHashSet<>();
    private final ObjectOpenHashSet<String> activeStages = new ObjectOpenHashSet<>();
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
        pendingFinalization = false;

        NeoForge.EVENT_BUS.post(new StageRegisterEndEvent(new StageScriptApi(this)));
    }

    public synchronized void clearRuntimeState() {
        currentStageId = 0;
        stageIds.clear();
        idToStage.clear();
        pendingStages.clear();
        activeStages.clear();
        collectingStages = false;
        pendingFinalization = false;
    }

    public synchronized void registerStage(String stageName) {
        if (!collectingStages) {
            throw new IllegalStateException("Stages can only be registered during StageRegisterEvent");
        }

        pendingStages.add(validateStageName(stageName));
    }

    public synchronized SendStagesToClientPacket createSyncPacket() {
        ObjectArrayList<SendStagesToClientPacket.StageEntry> stages = new ObjectArrayList<>(stageIds.size());
        ShortArrayList activeStageIds = new ShortArrayList(activeStages.size());

        for (Object2ShortMap.Entry<String> entry : getStageEntriesSortedById()) {
            short stageId = entry.getShortValue();
            String stageName = entry.getKey();
            stages.add(new SendStagesToClientPacket.StageEntry(stageId, stageName));

            if (activeStages.contains(stageName)) {
                activeStageIds.add(stageId);
            }
        }

        return new SendStagesToClientPacket(List.copyOf(stages), activeStageIds.toShortArray());
    }

    public synchronized void applySyncedStages(List<SendStagesToClientPacket.StageEntry> syncedStages, short[] syncedActiveStageIds) {
        clearRuntimeState();

        for (SendStagesToClientPacket.StageEntry stage : syncedStages) {
            putStage(stage.stageName(), stage.stageId());
        }

        for (short activeStageId : syncedActiveStageIds) {
            String activeStageName = idToStage.get(activeStageId);
            if (activeStageName != null) {
                activeStages.add(activeStageName);
            }
        }
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

    public synchronized short getId(String stageName) {
        return stageIds.getShort(stageName);
    }

    public synchronized short getIdOrThrow(String stageName) {
        String normalized = validateStageName(stageName);
        short stageId = stageIds.getShort(normalized);
        if (stageId == UNKNOWN_STAGE) {
            throw new IllegalArgumentException("Unknown stage '" + normalized + "'");
        }

        return stageId;
    }

    public synchronized boolean isKnownStage(String stageName) {
        return stageIds.containsKey(validateStageName(stageName));
    }

    public synchronized boolean isActiveStage(String stageName) {
        return activeStages.contains(validateStageName(stageName));
    }

    public synchronized String getName(short id) {
        return idToStage.get(id);
    }

    public synchronized int getRegisteredCount() {
        return currentStageId;
    }

    public synchronized List<String> getActiveStages() {
        ObjectArrayList<String> stages = new ObjectArrayList<>(activeStages);
        stages.sort(Comparator.naturalOrder());
        return List.copyOf(stages);
    }
}
