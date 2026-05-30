package dev.sixik.gpf;

import com.mojang.logging.LogUtils;
import dev.sixik.gpf.impl.network.GPFNetworking;
import dev.sixik.gpf.registry.StagesRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(GameProgressionFramework.MODID)
public class GameProgressionFramework {

    public static final String MODID = "game_progression_framework";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GameProgressionFramework(IEventBus modEventBus, ModContainer modContainer) {
        StagesRegistry.INSTANCE = new StagesRegistry();
        modEventBus.addListener(GPFNetworking::register);
    }
}
