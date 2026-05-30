package dev.sixik.gpf.impl.mixin;

import dev.sixik.gpf.registry.StagesRegistry;
import net.minecraft.server.WorldLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(WorldLoader.class)
public class MixinWorldLoader {

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static <U, T> CompletableFuture<U> gpf$load(CompletableFuture<U> instance, Function<? super T, ? extends U> fn, Executor executor) {
        return instance.thenApplyAsync((v) -> {
            StagesRegistry.INSTANCE.reloadFromScripts();
            return v;
        }, executor).thenApplyAsync((Function<? super U, ? extends U>) fn, executor);
    }
}
