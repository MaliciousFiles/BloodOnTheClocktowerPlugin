package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class MinecraftHook<D> implements Listener {
    private CompletableFuture<D> future;

    public MinecraftHook() {
        this.future = new CompletableFuture<>();
        Bukkit.getPluginManager().registerEvents(this, BloodOnTheClocktower.instance);
    }

    public final void complete(D data) {
        HandlerList.unregisterAll(this);
        future.complete(data);
    }

    public final D get() throws ExecutionException, InterruptedException {
        try {
            return future.get();
        } catch (CancellationException _) {
            return null;
        }
    }

    protected final void cancel() {
        future.cancel(true);
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }
}
