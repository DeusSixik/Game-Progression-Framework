package dev.sixik.gpf.api.event;

import dev.sixik.gpf.api.script.StageScriptApi;
import net.neoforged.bus.api.Event;

public final class StageRegisterFinalizeEvent extends Event {

    private final StageScriptApi stageScriptApi;

    public StageRegisterFinalizeEvent(StageScriptApi instance) {
        this.stageScriptApi = instance;
    }

    /**
     * @return the low-level script API used by this registration event
     */
    public StageScriptApi getStageScriptApi() {
        return stageScriptApi;
    }
}
