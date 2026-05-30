package dev.sixik.gpf.impl.network;

import dev.sixik.gpf.GameProgressionFramework;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class GPFNetworking {

    private GPFNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(GameProgressionFramework.MODID);
        registrar.playToClient(SendStagesToClientPacket.TYPE, SendStagesToClientPacket.STREAM_CODEC, SendStagesToClientPacket::handle);
        registrar.playToClient(SendPlayerStagesToClientPacket.TYPE, SendPlayerStagesToClientPacket.STREAM_CODEC, SendPlayerStagesToClientPacket::handle);
    }
}
