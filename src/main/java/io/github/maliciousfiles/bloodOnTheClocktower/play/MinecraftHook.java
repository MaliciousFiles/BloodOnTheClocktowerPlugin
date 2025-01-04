package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
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
        future.complete(data);
    }

    private PlayerAction cancelAction;
    public final MinecraftHook<D> cancellable(PlayerAction action, String cancelText) {
        if (cancelAction != null) throw new IllegalStateException("Already cancellable");

        cancelAction = action;
        action.enable(this::cancel, Component.text("Cancel").decorate(TextDecoration.BOLD)
                .append(Component.text(" - "+cancelText).decoration(TextDecoration.BOLD, false)));

        return this;
    }

    public final D get() throws ExecutionException, InterruptedException {
        D response = null;
        try {
            response = future.get();
        } catch (CancellationException | InterruptedException _) {}

        if (cancelAction != null) cancelAction.disable();
        HandlerList.unregisterAll(this);
        this.cleanup();
        return response;
    }

    protected abstract void cleanup();

    protected final void cancel() {
        future.cancel(true);
    }

    public final boolean isCancelled() {
        return future.isCancelled();
    }
}
