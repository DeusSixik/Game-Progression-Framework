package dev.sixik.gpf.impl.compat.kubejs.events;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;
import dev.sixik.gpf.api.event.StageRegisterEndEvent;

public final class KubeStageRegisterEndEvent implements KubeEvent {

    private final StageRegisterEndEvent internal;

    public KubeStageRegisterEndEvent(StageRegisterEndEvent internal) {
        this.internal = internal;
    }

    @Info("Returns true if the stage is present in the finalized registry.")
    public boolean isKnown(String stageName) {
        return internal.getScriptApi().isKnown(stageName);
    }

    @Info("Returns true if the stage is active for the current reload.")
    public boolean isActive(String stageName) {
        return internal.getScriptApi().isActive(stageName);
    }
}
