package io.github.maliciousfiles.bloodOnTheClocktower.play;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.concurrent.CompletableFuture;

public abstract class MinecraftHook<D> implements Listener {
    private final CompletableFuture<D> complete;

    public MinecraftHook(CompletableFuture<D> complete) {
        this.complete = complete;
    }

    protected final void complete(D data) {
        complete.complete(data);
        HandlerList.unregisterAll(this);
    }
}
