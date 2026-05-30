package dev.sixik.gpf.api.event;

import dev.sixik.gpf.api.script.StageScriptApi;
import net.neoforged.bus.api.Event;

public final class StageRegisterEndEvent extends Event {

    private final StageScriptApi scriptApi;

    public StageRegisterEndEvent(StageScriptApi scriptApi) {
        this.scriptApi = scriptApi;
    }

    public StageScriptApi getScriptApi() {
        return scriptApi;
    }
}
