package dev.sixik.gpf.impl.mixin;

import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Redirect(method = "reloadResources", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAcceptAsync(Ljava/util/function/Consumer;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    public <T> CompletableFuture<Void> gpf$reloadResources(CompletableFuture instance, Consumer<? super T> action, Executor executor) {
        return instance.thenAcceptAsync((v) ->  StagesRegistry.INSTANCE.reloadFromScripts(), executor).thenAcceptAsync(action, executor);
    }
}
