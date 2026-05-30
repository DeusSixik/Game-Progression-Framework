package dev.sixik.gpf.impl.compat.kubejs;

import dev.sixik.gpf.api.Stages;
import net.minecraft.server.level.ServerPlayer;

public final class GPFKubeJS {

    private GPFKubeJS() {
    }

    public static short getStageId(String stageName) {
        return Stages.getStageId(stageName);
    }

    public static boolean hasStageSlow(String stageName, ServerPlayer player) {
        return Stages.hasStageSlow(stageName, player);
    }

    public static boolean hasStage(short stage, ServerPlayer player) {
        return Stages.hasStage(stage, player);
    }

    public static boolean hasStages(ServerPlayer player, short... stages) {
        return Stages.hasStages(player, stages);
    }

    public static boolean hasStagesSlow(ServerPlayer player, String... stageNames) {
        return Stages.hasStagesSlow(player, stageNames);
    }

    public static boolean hasAnyStage(ServerPlayer player, short... stages) {
        return Stages.hasAnyStage(player, stages);
    }

    public static boolean hasAnyStageSlow(ServerPlayer player, String... stageNames) {
        return Stages.hasAnyStageSlow(player, stageNames);
    }

    public static boolean addStageSlow(String stageName, ServerPlayer player) {
        return Stages.addStageSlow(stageName, player);
    }

    public static boolean addStage(short stage, ServerPlayer player) {
        return Stages.addStage(stage, player);
    }

    public static boolean removeStageSlow(String stageName, ServerPlayer player) {
        return Stages.removeStageSlow(stageName, player);
    }

    public static boolean removeStage(short stage, ServerPlayer player) {
        return Stages.removeStage(stage, player);
    }

    public static boolean clearStages(ServerPlayer player) {
        return Stages.clearStages(player);
    }
}
