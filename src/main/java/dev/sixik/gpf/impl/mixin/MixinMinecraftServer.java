package dev.sixik.gpf.impl.mixin;

import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Inject(method = "reloadResources", at = @At(value = "RETURN"))
    public void gpf$reloadResources(Collection<String> selectedIds, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        StagesRegistry.INSTANCE.reloadFromScripts();
    }
}
