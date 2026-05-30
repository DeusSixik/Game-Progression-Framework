package dev.sixik.gpf.impl.server;

import dev.sixik.gpf.api.StageData;
import dev.sixik.gpf.api.progression.PlayerProgression;
import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class ServerPlayerProgression implements PlayerProgression {

    private final ServerPlayer player;

    public ServerPlayerProgression(ServerPlayer player) {
        this.player = Objects.requireNonNull(player, "Player cannot be null");
    }

    @Override
    public ServerPlayer getPlayer() {
        return player;
    }

    @Override
    public StageData snapshot() {
        return PlayerStageDataService.copy(player);
    }

    @Override
    public boolean hasStage(String stageName) {
        return hasStage(getStageId(stageName));
    }

    @Override
    public boolean hasStage(short stageId) {
        validateStageId(stageId);
        return PlayerStageDataService.hasStage(player, stageId);
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
    public boolean hasStageFast(short stageId) {
        return PlayerStageDataService.hasStage(player, stageId);
    }

    @Override
    public boolean hasStagesFast(short... stageIds) {
        return PlayerStageDataService.hasStages(player, stageIds);
    }

    @Override
    public boolean addStage(String stageName) {
        return addStage(getStageId(stageName));
    }

    @Override
    public boolean addStage(short stageId) {
        validateStageId(stageId);
        return PlayerStageDataService.addStage(player, stageId);
    }

    @Override
    public boolean addStageFast(short stageId) {
        return PlayerStageDataService.addStage(player, stageId);
    }

    @Override
    public boolean removeStage(String stageName) {
        return removeStage(getStageId(stageName));
    }

    @Override
    public boolean removeStage(short stageId) {
        validateStageId(stageId);
        return PlayerStageDataService.removeStage(player, stageId);
    }

    @Override
    public boolean removeStageFast(short stageId) {
        return PlayerStageDataService.removeStage(player, stageId);
    }

    @Override
    public boolean clearStages() {
        return PlayerStageDataService.clearStages(player);
    }

    @Override
    public boolean setStages(StageData stageData) {
        return PlayerStageDataService.setStages(player, stageData);
    }

    @Override
    public boolean mergeStages(StageData stageData) {
        return PlayerStageDataService.mergeStages(player, stageData);
    }

    @Override
    public boolean replaceRawStages(long[] rawStages) {
        return PlayerStageDataService.replace(player, rawStages);
    }

    @Override
    public void sync() {
        PlayerStageDataService.sync(player);
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
