package dev.sixik.gpf.impl.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import dev.sixik.gpf.impl.server.PlayerStageDataService;
import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.level.ServerPlayer;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.gpf.api.Stages")
public class GPFCraftTweaker {

    @ZenCodeType.Method
    public static short getStageId(String stageName) {
        return StagesRegistry.INSTANCE.getIdOrThrow(stageName);
    }

    @ZenCodeType.Method
    public static boolean hasStageSlow(String stageName, ServerPlayer player) {
        return PlayerStageDataService.getOrCreate(player).hasStageSlow(stageName);
    }

    @ZenCodeType.Method
    public static boolean hasStage(short stage, ServerPlayer player) {
        return PlayerStageDataService.getOrCreate(player).hasStage(stage);
    }

    @ZenCodeType.Method
    public static void addStageSlow(String stageName, ServerPlayer player) {
        PlayerStageDataService.addStage(player, getStageId(stageName));
    }

    @ZenCodeType.Method
    public static void addStage(short stage, ServerPlayer player) {
        PlayerStageDataService.addStage(player, stage);
    }

    @ZenCodeType.Method
    public static boolean removeStageSlow(String stageName, ServerPlayer player) {
       return PlayerStageDataService.removeStage(player, getStageId(stageName));
    }

    @ZenCodeType.Method
    public static boolean removeStage(short stage, ServerPlayer player) {
       return PlayerStageDataService.removeStage(player, stage);
    }

    @ZenCodeType.Method
    public static void clearStages(ServerPlayer player) {
        PlayerStageDataService.getOrCreate(player).clearAllStages();
    }
}
