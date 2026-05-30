package dev.sixik.gpf.api.event;

import dev.sixik.gpf.api.script.StageScriptApi;
import net.neoforged.bus.api.Event;

/**
 * Fired during stage collection so scripts and integrations can register stages.
 */
public final class StageRegisterEvent extends Event {

    private final StageScriptApi scriptApi;

    public StageRegisterEvent(StageScriptApi scriptApi) {
        this.scriptApi = scriptApi;
    }

    /**
     * Registers a stage during the current collection pass.
     *
     * @param stageName the normalized stage name to register
     */
    public void registerStage(String stageName) {
        scriptApi.register(stageName);
    }

    /**
     * @return the low-level script API used by this registration event
     */
    public StageScriptApi getScriptApi() {
        return scriptApi;
    }
}
