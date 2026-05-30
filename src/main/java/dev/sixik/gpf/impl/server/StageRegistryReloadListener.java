package dev.sixik.gpf.impl.server;

import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class StageRegistryReloadListener extends SimplePreparableReloadListener<Void> {

    public static final StageRegistryReloadListener INSTANCE = new StageRegistryReloadListener();

    private StageRegistryReloadListener() {
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
        StagesRegistry.INSTANCE.reloadFromScripts();
    }


}
