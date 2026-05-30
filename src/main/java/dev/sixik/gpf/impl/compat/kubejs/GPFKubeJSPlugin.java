package dev.sixik.gpf.impl.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.sixik.gpf.impl.compat.kubejs.events.GPFKubeJSEvents;
import net.neoforged.neoforge.common.NeoForge;

public final class GPFKubeJSPlugin implements KubeJSPlugin {

    private static boolean registeredToEventBus;

    @Override
    public void init() {
        if (!registeredToEventBus) {
            registeredToEventBus = true;
            NeoForge.EVENT_BUS.register(new GPFKubeJSEventBridge());
        }
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(GPFKubeJSEvents.GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        if(bindings.type().isServer()) {
            bindings.add("GPFStages", GPFKubeJS.class);
        }
    }
}
