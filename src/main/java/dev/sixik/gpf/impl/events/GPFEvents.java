package dev.sixik.gpf.impl.events;

import dev.sixik.gpf.GameProgressionFramework;
import dev.sixik.gpf.impl.network.SendPlayerStagesToClientPacket;
import dev.sixik.gpf.impl.server.StageRegistryReloadListener;
import dev.sixik.gpf.impl.server.PlayerStageDataService;
import dev.sixik.gpf.impl.network.SendStagesToClientPacket;
import dev.sixik.gpf.registry.StagesRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
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
    public static void onServerStarted(ServerStartedEvent event) {
        StagesRegistry.INSTANCE.finalizePendingStages(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        StagesRegistry.INSTANCE.clearRuntimeState();
    }
}
