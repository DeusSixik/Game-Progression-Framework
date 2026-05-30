package dev.sixik.gpf.api.script;

import dev.sixik.gpf.registry.StagesRegistry;

public final class StageScriptApi {

    private final StagesRegistry registry;

    public StageScriptApi(StagesRegistry registry) {
        this.registry = registry;
    }

    public void register(String stageName) {
        registry.registerStage(stageName);
    }

    public boolean isKnown(String stageName) {
        return registry.isKnownStage(stageName);
    }

    public boolean isActive(String stageName) {
        return registry.isActiveStage(stageName);
    }
}
