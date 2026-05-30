package dev.sixik.gpf.api.event;

import dev.sixik.gpf.api.script.StageScriptApi;
import net.neoforged.bus.api.Event;

/**
 * Fired after stage collection has been finalized for the current server state.
 */
public final class StageRegisterEndEvent extends Event {

    private final StageScriptApi scriptApi;

    public StageRegisterEndEvent(StageScriptApi scriptApi) {
        this.scriptApi = scriptApi;
    }

    /**
     * @return the script API associated with the completed registration pass
     */
    public StageScriptApi getScriptApi() {
        return scriptApi;
    }
}
