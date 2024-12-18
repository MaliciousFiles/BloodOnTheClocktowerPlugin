package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.concurrent.CompletableFuture;

public abstract class MinecraftHook<E extends Event, D> implements Listener {

    private final CompletableFuture<D> complete;

    public MinecraftHook(Class<E> event, EventPriority priority, boolean ignoreCancelled, CompletableFuture<D> complete) {
        this.complete = complete;
        Bukkit.getPluginManager().registerEvent(event, this,
                priority, this::onEventInternal, BloodOnTheClocktower.instance, ignoreCancelled);
    }

    public MinecraftHook(Class<E> event, CompletableFuture<D> complete) {
        this(event, EventPriority.NORMAL, false, complete);
    }

    private void onEventInternal(Listener listener, Event event) {
        onEvent((E) event);
    }

    protected abstract void onEvent(E event);

    protected void complete(D data) { complete.complete(data); }
}
