package dev.sixik.gpf.api.script;

import dev.sixik.gpf.registry.StagesRegistry;

/**
 * Script-facing view of the stage registry used during reload-time registration.
 */
public final class StageScriptApi {

    private final StagesRegistry registry;

    public StageScriptApi(StagesRegistry registry) {
        this.registry = registry;
    }

    /**
     * Registers a stage for the current reload pass.
     *
     * @param stageName the normalized stage name to register
     */
    public void register(String stageName) {
        registry.registerStage(stageName);
    }

    /**
     * Checks whether a validated stage is known by the registry.
     *
     * @param stageName the normalized stage name
     * @return {@code true} when the stage already exists in the persistent registry
     */
    public boolean isKnown(String stageName) {
        return registry.isKnownStage(stageName);
    }

    /**
     * Checks whether a validated stage is active in the current reload result.
     *
     * @param stageName the normalized stage name
     * @return {@code true} when the stage is active for the current server state
     */
    public boolean isActive(String stageName) {
        return registry.isActiveStage(stageName);
    }
}
