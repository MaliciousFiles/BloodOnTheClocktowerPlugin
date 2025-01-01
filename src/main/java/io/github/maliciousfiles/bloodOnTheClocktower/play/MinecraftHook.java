package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class MinecraftHook<D> implements Listener {
    private final CompletableFuture<D> complete;

    public MinecraftHook() {
        this.complete = new CompletableFuture<>();
        Bukkit.getPluginManager().registerEvents(this, BloodOnTheClocktower.instance);
    }

    public D get() throws ExecutionException, InterruptedException {
        return complete.get();
    }

    protected final void complete(D data) {
        complete.complete(data);
        HandlerList.unregisterAll(this);
    }
}
