package dev.sixik.gpf.impl.server;

import dev.sixik.gpf.api.StageData;
import dev.sixik.gpf.api.progression.PlayerProgression;
import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public final class ServerPlayerProgression implements PlayerProgression {

    private final UUID playerId;

    public ServerPlayerProgression(UUID playerId) {
        this.playerId = Objects.requireNonNull(playerId, "Player id cannot be null");
    }

    public ServerPlayerProgression(ServerPlayer player) {
        this(Objects.requireNonNull(player, "Player cannot be null").getUUID());
    }

    @Override
    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    @Nullable
    public ServerPlayer getPlayer() {
        return PlayerStageDataService.getOnlinePlayer(playerId);
    }

    @Override
    public StageData snapshot() {
        return PlayerStageDataService.copy(playerId);
    }

    @Override
    public boolean hasStage(String stageName) {
        return hasStage(getStageId(stageName));
    }

    @Override
    public boolean hasStage(short stageId) {
        validateStageId(stageId);
        return PlayerStageDataService.hasStage(playerId, stageId);
    }

    @Override
    public boolean hasStages(String... stageNames) {
        short[] stageIds = new short[stageNames.length];
        for (int i = 0; i < stageNames.length; i++) {
            stageIds[i] = getStageId(stageNames[i]);
        }

        return hasStages(stageIds);
    }

    @Override
    public boolean hasStages(short... stageIds) {
        for (short stageId : stageIds) {
            validateStageId(stageId);
        }

        return hasStagesFast(stageIds);
    }

    @Override
    public boolean hasAnyStage(String... stageNames) {
        short[] stageIds = new short[stageNames.length];
        for (int i = 0; i < stageNames.length; i++) {
            stageIds[i] = getStageId(stageNames[i]);
        }

        return hasAnyStage(stageIds);
    }

    @Override
    public boolean hasAnyStage(short... stageIds) {
        for (short stageId : stageIds) {
            validateStageId(stageId);
        }

        return hasAnyStageFast(stageIds);
    }

    @Override
    public boolean hasStageFast(short stageId) {
        return PlayerStageDataService.hasStage(playerId, stageId);
    }

    @Override
    public boolean hasStagesFast(short... stageIds) {
        return PlayerStageDataService.hasStages(playerId, stageIds);
    }

    @Override
    public boolean hasAnyStageFast(short... stageIds) {
        return PlayerStageDataService.hasAnyStage(playerId, stageIds);
    }

    @Override
    public boolean addStage(String stageName) {
        return addStage(getStageId(stageName));
    }

    @Override
    public boolean addStage(short stageId) {
        validateStageId(stageId);
        return PlayerStageDataService.addStage(playerId, stageId);
    }

    @Override
    public boolean addStageFast(short stageId) {
        return PlayerStageDataService.addStage(playerId, stageId);
    }

    @Override
    public boolean removeStage(String stageName) {
        return removeStage(getStageId(stageName));
    }

    @Override
    public boolean removeStage(short stageId) {
        validateStageId(stageId);
        return PlayerStageDataService.removeStage(playerId, stageId);
    }

    @Override
    public boolean removeStageFast(short stageId) {
        return PlayerStageDataService.removeStage(playerId, stageId);
    }

    @Override
    public boolean clearStages() {
        return PlayerStageDataService.clearStages(playerId);
    }

    @Override
    public boolean setStages(StageData stageData) {
        return PlayerStageDataService.setStages(playerId, stageData);
    }

    @Override
    public boolean mergeStages(StageData stageData) {
        return PlayerStageDataService.mergeStages(playerId, stageData);
    }

    @Override
    public boolean replaceRawStages(long[] rawStages) {
        return PlayerStageDataService.replace(playerId, rawStages);
    }

    @Override
    public void sync() {
        PlayerStageDataService.sync(playerId);
    }

    private short getStageId(String stageName) {
        return StagesRegistry.INSTANCE.getIdOrThrow(stageName);
    }

    private void validateStageId(short stageId) {
        if (StagesRegistry.INSTANCE.getName(stageId) == null) {
            throw new IllegalArgumentException("Unknown stage id '" + stageId + "'");
        }
    }
}
