package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Option;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetChoiceHook<D> extends MinecraftHook<D> {
    public GetChoiceHook(List<Option<D>> options, CompletableFuture<D> complete) {
        super(complete);

        // TODO: create inventory, implement handler
    }

    @EventHandler
    protected void onEvent(InventoryClickEvent event) {}
}
