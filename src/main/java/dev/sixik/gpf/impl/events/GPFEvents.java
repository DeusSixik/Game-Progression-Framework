package dev.sixik.gpf.impl.events;

import dev.sixik.gpf.GameProgressionFramework;
import dev.sixik.gpf.impl.server.command.GPFStagesCommand;
import dev.sixik.gpf.impl.network.SendPlayerStagesToClientPacket;
import dev.sixik.gpf.impl.server.PlayerStageDataService;
import dev.sixik.gpf.impl.network.SendStagesToClientPacket;
import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = GameProgressionFramework.MODID)
public class GPFEvents {

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        SendStagesToClientPacket packet = StagesRegistry.INSTANCE.createSyncPacket();
        event.getRelevantPlayers().forEach(player -> {
            SendPlayerStagesToClientPacket playerPacket = PlayerStageDataService.createSyncPacket(player);
            PacketDistributor.sendToPlayer(player, packet, playerPacket);
        });
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        GPFStagesCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        PlayerStageDataService.initialize(event.getServer());
        StagesRegistry.INSTANCE.finalizePendingStages(event.getServer());
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.OVERWORLD) {
            PlayerStageDataService.flushDirty();
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PlayerStageDataService.flushDirty();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PlayerStageDataService.shutdown();
        StagesRegistry.INSTANCE.clearRuntimeState();
    }
}
