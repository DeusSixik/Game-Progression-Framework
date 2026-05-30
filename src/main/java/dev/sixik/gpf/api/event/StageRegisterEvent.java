package dev.sixik.gpf.api.event;

import dev.sixik.gpf.api.script.StageScriptApi;
import net.neoforged.bus.api.Event;

public final class StageRegisterEvent extends Event {

    private final StageScriptApi scriptApi;

    public StageRegisterEvent(StageScriptApi scriptApi) {
        this.scriptApi = scriptApi;
    }

    public void registerStage(String stageName) {
        scriptApi.register(stageName);
    }

    public StageScriptApi getScriptApi() {
        return scriptApi;
    }
}
