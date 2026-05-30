package dev.sixik.gpf.impl.compat.kubejs.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface GPFKubeJSEvents {

    EventGroup GROUP = EventGroup.of("GPFEvents");

    EventHandler STAGE_REGISTER = GROUP.server("stageRegister", () -> KubeStageRegisterEvent.class);
    EventHandler STAGE_REGISTER_END = GROUP.server("stageRegisterEnd", () -> KubeStageRegisterEndEvent.class);
}
