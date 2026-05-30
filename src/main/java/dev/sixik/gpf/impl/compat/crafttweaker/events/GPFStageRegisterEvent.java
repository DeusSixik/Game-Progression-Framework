package dev.sixik.gpf.impl.compat.crafttweaker.events;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.event.ZenEvent;
import com.blamejared.crafttweaker.api.event.bus.IEventBus;
import com.blamejared.crafttweaker.api.event.bus.NeoForgeEventBusWire;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import dev.sixik.gpf.api.event.StageRegisterEvent;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenEvent
@NativeTypeRegistration(value = StageRegisterEvent.class, zenCodeName = "mods.gpf.api.events.StageRegisterEvent")
public class GPFStageRegisterEvent {

    @ZenEvent.Bus
    public static final IEventBus<StageRegisterEvent> BUS = IEventBus.direct(
            StageRegisterEvent.class,
            NeoForgeEventBusWire.of()
    );

    @ZenCodeType.Method
    public static void registerStage(StageRegisterEvent internal, String stageName) {
        internal.registerStage(stageName);
    }

    @ZenCodeType.Method
    public static void register(StageRegisterEvent internal, String stageName) {
        internal.registerStage(stageName);
    }
}
