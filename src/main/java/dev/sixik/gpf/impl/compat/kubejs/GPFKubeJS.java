package dev.sixik.gpf.impl.compat.kubejs;

import dev.sixik.gpf.impl.server.PlayerStageDataService;
import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.level.ServerPlayer;

public final class GPFKubeJS {

    private GPFKubeJS() {
    }

    public static short getStageId(String stageName) {
        return StagesRegistry.INSTANCE.getIdOrThrow(stageName);
    }

    public static boolean hasStageSlow(String stageName, ServerPlayer player) {
        return PlayerStageDataService.getOrCreate(player).hasStageSlow(stageName);
    }

    public static boolean hasStage(short stage, ServerPlayer player) {
        return PlayerStageDataService.getOrCreate(player).hasStage(stage);
    }

    public static void addStageSlow(String stageName, ServerPlayer player) {
        PlayerStageDataService.addStage(player, getStageId(stageName));
    }

    public static void addStage(short stage, ServerPlayer player) {
        PlayerStageDataService.addStage(player, stage);
    }

    public static boolean removeStageSlow(String stageName, ServerPlayer player) {
        return PlayerStageDataService.removeStage(player, getStageId(stageName));
    }

    public static boolean removeStage(short stage, ServerPlayer player) {
        return PlayerStageDataService.removeStage(player, stage);
    }

    public static void clearStages(ServerPlayer player) {
        PlayerStageDataService.replace(player, new long[0]);
    }
}
