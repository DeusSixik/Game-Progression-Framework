package dev.sixik.gpf.impl.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.sixik.gpf.api.StageData;
import dev.sixik.gpf.api.Stages;
import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class GPFStagesCommand {

    private GPFStagesCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gpfStages")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("add")
                        .then(Commands.argument("stage", StringArgumentType.word())
                                .suggests(GPFStagesCommand::suggestStages)
                                .executes(context -> addStageToSelf(context, getStageName(context))))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("stage", StringArgumentType.word())
                                        .suggests(GPFStagesCommand::suggestStages)
                                        .executes(context -> addStage(EntityArgument.getPlayer(context, "player"), getStageName(context), context.getSource())))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("stage", StringArgumentType.word())
                                .suggests(GPFStagesCommand::suggestStages)
                                .executes(context -> removeStageFromSelf(context, getStageName(context))))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("stage", StringArgumentType.word())
                                        .suggests(GPFStagesCommand::suggestStages)
                                        .executes(context -> removeStage(EntityArgument.getPlayer(context, "player"), getStageName(context), context.getSource())))))
                .then(Commands.literal("clear")
                        .executes(GPFStagesCommand::clearSelf)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> clearStages(EntityArgument.getPlayer(context, "player"), context.getSource()))))
                .then(Commands.literal("list")
                        .executes(GPFStagesCommand::listSelf)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> listStages(EntityArgument.getPlayer(context, "player"), context.getSource()))))
                .then(Commands.literal("has")
                        .then(Commands.argument("stages", StringArgumentType.greedyString())
                                .suggests(GPFStagesCommand::suggestStageList)
                                .executes(context -> hasAnyStages(context.getSource().getPlayerOrException(), getStageNames(context), context.getSource())))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("stages", StringArgumentType.greedyString())
                                        .suggests(GPFStagesCommand::suggestStageList)
                                        .executes(context -> hasAnyStages(EntityArgument.getPlayer(context, "player"), getStageNames(context), context.getSource()))))));
    }

    private static int addStageToSelf(CommandContext<CommandSourceStack> context, String stageName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return addStage(context.getSource().getPlayerOrException(), stageName, context.getSource());
    }

    private static int removeStageFromSelf(CommandContext<CommandSourceStack> context, String stageName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return removeStage(context.getSource().getPlayerOrException(), stageName, context.getSource());
    }

    private static int clearSelf(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return clearStages(context.getSource().getPlayerOrException(), context.getSource());
    }

    private static int listSelf(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return listStages(context.getSource().getPlayerOrException(), context.getSource());
    }

    private static int hasAnyStages(ServerPlayer player, String[] stageNames, CommandSourceStack source) {
        if (stageNames.length == 0) {
            source.sendFailure(Component.literal("You must provide at least one stage."));
            return 0;
        }

        short[] stageIds = resolveStageIds(source, stageNames);
        if (stageIds == null) {
            return 0;
        }

        List<String> matchedStages = getMatchingStages(player, stageNames, stageIds);
        if (matchedStages.isEmpty()) {
            source.sendFailure(Component.literal(player.getGameProfile().getName() + " has none of the provided stages: " + String.join(", ", stageNames)));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " has " + matchedStages.size() + " matching stage(s): " + String.join(", ", matchedStages)), false);
        return matchedStages.size();
    }

    private static int addStage(ServerPlayer player, String stageName, CommandSourceStack source) {
        short stageId = resolveStageId(source, stageName);
        if (stageId == StagesRegistry.UNKNOWN_STAGE) {
            return 0;
        }

        boolean changed = Stages.addStageFast(stageId, player);
        if (changed) {
            source.sendSuccess(() -> Component.literal("Added stage '" + stageName + "' to " + player.getGameProfile().getName() + "."), true);
            return 1;
        }

        source.sendFailure(Component.literal(player.getGameProfile().getName() + " already has stage '" + stageName + "'."));
        return 0;
    }

    private static int removeStage(ServerPlayer player, String stageName, CommandSourceStack source) {
        short stageId = resolveStageId(source, stageName);
        if (stageId == StagesRegistry.UNKNOWN_STAGE) {
            return 0;
        }

        boolean changed = Stages.removeStageFast(stageId, player);
        if (changed) {
            source.sendSuccess(() -> Component.literal("Removed stage '" + stageName + "' from " + player.getGameProfile().getName() + "."), true);
            return 1;
        }

        source.sendFailure(Component.literal(player.getGameProfile().getName() + " does not have stage '" + stageName + "'."));
        return 0;
    }

    private static int clearStages(ServerPlayer player, CommandSourceStack source) {
        boolean changed = Stages.clearStages(player);
        if (changed) {
            source.sendSuccess(() -> Component.literal("Cleared all stages for " + player.getGameProfile().getName() + "."), true);
            return 1;
        }

        source.sendFailure(Component.literal(player.getGameProfile().getName() + " has no stages to clear."));
        return 0;
    }

    private static int listStages(ServerPlayer player, CommandSourceStack source) {
        List<String> stageNames = getPlayerStageNames(player);
        if (stageNames.isEmpty()) {
            source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " has no stages."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " stages (" + stageNames.size() + "): " + String.join(", ", stageNames)), false);
        return stageNames.size();
    }

    private static short resolveStageId(CommandSourceStack source, String stageName) {
        short stageId = Stages.getStageIdFast(stageName);
        if (stageId == StagesRegistry.UNKNOWN_STAGE) {
            source.sendFailure(Component.literal("Unknown stage '" + stageName + "'."));
        }

        return stageId;
    }

    private static short[] resolveStageIds(CommandSourceStack source, String[] stageNames) {
        short[] stageIds = new short[stageNames.length];
        for (int i = 0; i < stageNames.length; i++) {
            short stageId = resolveStageId(source, stageNames[i]);
            if (stageId == StagesRegistry.UNKNOWN_STAGE) {
                return null;
            }

            stageIds[i] = stageId;
        }

        return stageIds;
    }

    private static String getStageName(CommandContext<CommandSourceStack> context) {
        return StringArgumentType.getString(context, "stage");
    }

    private static String[] getStageNames(CommandContext<CommandSourceStack> context) {
        String rawStages = StringArgumentType.getString(context, "stages").trim();
        if (rawStages.isEmpty()) {
            return new String[0];
        }

        return rawStages.split("\\s+");
    }

    private static CompletableFuture<Suggestions> suggestStages(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(StagesRegistry.INSTANCE.getRegisteredStages(), builder);
    }

    private static CompletableFuture<Suggestions> suggestStageList(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        int lastSpaceIndex = remaining.lastIndexOf(' ');
        String prefix = lastSpaceIndex >= 0 ? remaining.substring(0, lastSpaceIndex + 1) : "";
        String currentToken = lastSpaceIndex >= 0 ? remaining.substring(lastSpaceIndex + 1) : remaining;

        Set<String> usedStages = new LinkedHashSet<>();
        if (!prefix.isEmpty()) {
            String[] tokens = prefix.trim().split("\\s+");
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    usedStages.add(token);
                }
            }
        }

        SuggestionsBuilder offsetBuilder = builder.createOffset(builder.getStart() + prefix.length());
        for (String stageName : StagesRegistry.INSTANCE.getRegisteredStages()) {
            if (!usedStages.contains(stageName) && stageName.startsWith(currentToken)) {
                offsetBuilder.suggest(stageName);
            }
        }

        return offsetBuilder.buildFuture();
    }

    private static List<String> getPlayerStageNames(ServerPlayer player) {
        StageData stageData = Stages.snapshot(player);
        BitSet bits = BitSet.valueOf(stageData.toRawData());
        List<String> stageNames = new ArrayList<>(bits.cardinality());

        for (int stageId = bits.nextSetBit(0); stageId >= 0; stageId = bits.nextSetBit(stageId + 1)) {
            String stageName = Stages.getStageNameOrNull((short) stageId);
            stageNames.add(stageName != null ? stageName : "unknown:" + stageId);
        }

        return stageNames;
    }

    private static List<String> getMatchingStages(ServerPlayer player, String[] stageNames, short[] stageIds) {
        List<String> matchedStages = new ArrayList<>(stageIds.length);
        StageData stageData = Stages.snapshot(player);

        for (int i = 0; i < stageIds.length; i++) {
            if (stageData.hasStage(stageIds[i])) {
                matchedStages.add(stageNames[i]);
            }
        }

        return matchedStages;
    }
}
