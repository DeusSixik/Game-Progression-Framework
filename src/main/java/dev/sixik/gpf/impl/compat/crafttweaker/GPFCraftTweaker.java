package dev.sixik.gpf.impl.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import dev.sixik.gpf.api.Stages;
import net.minecraft.server.level.ServerPlayer;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.gpf.api.Stages")
public class GPFCraftTweaker {

    @ZenCodeType.Method
    public static short getStageId(String stageName) {
        return Stages.getStageId(stageName);
    }

    @ZenCodeType.Method
    public static boolean hasStageSlow(String stageName, ServerPlayer player) {
        return Stages.hasStageSlow(stageName, player);
    }

    @ZenCodeType.Method
    public static boolean hasStage(short stage, ServerPlayer player) {
        return Stages.hasStage(stage, player);
    }

    @ZenCodeType.Method
    public static boolean hasStages(ServerPlayer player, short... stages) {
        return Stages.hasStages(player, stages);
    }

    @ZenCodeType.Method
    public static boolean hasStagesSlow(ServerPlayer player, String... stageNames) {
        return Stages.hasStagesSlow(player, stageNames);
    }

    @ZenCodeType.Method
    public static boolean hasAnyStage(ServerPlayer player, short... stages) {
        return Stages.hasAnyStage(player, stages);
    }

    @ZenCodeType.Method
    public static boolean hasAnyStageSlow(ServerPlayer player, String... stageNames) {
        return Stages.hasAnyStageSlow(player, stageNames);
    }

    @ZenCodeType.Method
    public static boolean addStageSlow(String stageName, ServerPlayer player) {
        return Stages.addStageSlow(stageName, player);
    }

    @ZenCodeType.Method
    public static boolean addStage(short stage, ServerPlayer player) {
        return Stages.addStage(stage, player);
    }

    @ZenCodeType.Method
    public static boolean removeStageSlow(String stageName, ServerPlayer player) {
       return Stages.removeStageSlow(stageName, player);
    }

    @ZenCodeType.Method
    public static boolean removeStage(short stage, ServerPlayer player) {
       return Stages.removeStage(stage, player);
    }

    @ZenCodeType.Method
    public static boolean clearStages(ServerPlayer player) {
        return Stages.clearStages(player);
    }
}
