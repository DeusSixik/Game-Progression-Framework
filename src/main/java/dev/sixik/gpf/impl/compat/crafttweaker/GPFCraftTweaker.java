package dev.sixik.gpf.impl.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.Document;
import dev.sixik.gpf.api.Stages;
import net.minecraft.server.level.ServerPlayer;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.gpf.api.Stages")
@Document(value = "mods/gameprogrssion/api/Stages")
public class GPFCraftTweaker {

    /**
     * Resolves a validated stage name to its persistent id.
     *
     * @param stageName the normalized stage name
     * @return the persistent stage id
     */
    @ZenCodeType.Method
    public static short getStageId(String stageName) {
        return Stages.getStageId(stageName);
    }

    /**
     * Resolves a stage name and checks whether the owner has it.
     *
     * @param player   the owner to query
     * @param stageName the normalized stage name
     * @return {@code true} when the owner has the stage
     */
    @ZenCodeType.Method
    public static boolean hasStageSlow(String stageName, ServerPlayer player) {
        return Stages.hasStageSlow(stageName, player);
    }

    /**
     * Checks whether the owner has the provided trusted stage id without validating it first.
     *
     * @param player the owner to query
     * @param stage the trusted stage id to check
     * @return {@code true} when the owner has the stage
     */
    @ZenCodeType.Method
    public static boolean hasStage(short stage, ServerPlayer player) {
        return Stages.hasStage(stage, player);
    }

    /**
     * Checks whether the owner has every provided stage id.
     *
     * @param player  the owner to query
     * @param stages the stage ids to check
     * @return {@code true} when the owner has all provided stages
     */
    @ZenCodeType.Method
    public static boolean hasStages(ServerPlayer player, short... stages) {
        return Stages.hasStages(player, stages);
    }

    /**
     * Resolves all provided stage names and checks whether the owner has every one of them.
     *
     * @param player    the owner to query
     * @param stageNames the normalized stage names to check
     * @return {@code true} when the owner has all provided stages
     */
    @ZenCodeType.Method
    public static boolean hasStagesSlow(ServerPlayer player, String... stageNames) {
        return Stages.hasStagesSlow(player, stageNames);
    }

    /**
     * Checks whether the owner has at least one of the provided stage ids.
     *
     * @param player  the owner to query
     * @param stages the stage ids to check
     * @return {@code true} when the owner has any provided stage
     */
    @ZenCodeType.Method
    public static boolean hasAnyStage(ServerPlayer player, short... stages) {
        return Stages.hasAnyStage(player, stages);
    }

    /**
     * Resolves all provided stage names and checks whether the owner has at least one of them.
     *
     * @param player    the owner to query
     * @param stageNames the normalized stage names to check
     * @return {@code true} when the owner has any provided stage
     */
    @ZenCodeType.Method
    public static boolean hasAnyStageSlow(ServerPlayer player, String... stageNames) {
        return Stages.hasAnyStageSlow(player, stageNames);
    }

    /**
     * Resolves a stage name and adds it to the owner.
     *
     * @param player   the target owner
     * @param stageName the normalized stage name
     * @return {@code true} when the owner gained the stage
     */
    @ZenCodeType.Method
    public static boolean addStageSlow(String stageName, ServerPlayer player) {
        return Stages.addStageSlow(stageName, player);
    }

    /**
     * Adds a stage to the owner.
     *
     * @param player the target owner
     * @param stage the stage id to add
     * @return {@code true} when the owner gained the stage
     */
    @ZenCodeType.Method
    public static boolean addStage(short stage, ServerPlayer player) {
        return Stages.addStage(stage, player);
    }


    /**
     * Resolves a stage name and removes it from the owner.
     *
     * @param player   the target owner
     * @param stageName the normalized stage name
     * @return {@code true} when the owner lost the stage
     */
    @ZenCodeType.Method
    public static boolean removeStageSlow(String stageName, ServerPlayer player) {
       return Stages.removeStageSlow(stageName, player);
    }

    /**
     * Removes a stage from the owner.
     *
     * @param player the target owner
     * @param stage the stage id to remove
     * @return {@code true} when the owner lost the stage
     */
    @ZenCodeType.Method
    public static boolean removeStage(short stage, ServerPlayer player) {
       return Stages.removeStage(stage, player);
    }
    
    /**
     * Clears all stages from the owner.
     *
     * @param player the target owner
     * @return {@code true} when any stage was removed
     */
    @ZenCodeType.Method
    public static boolean clearStages(ServerPlayer player) {
        return Stages.clearStages(player);
    }
}
