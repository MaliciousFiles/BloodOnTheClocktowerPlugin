package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Option;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetChoiceHook<D> extends MinecraftHook<InventoryClickEvent, D> {
    public GetChoiceHook(List<Option<D>> options, CompletableFuture<D> complete) {
        super(InventoryClickEvent.class, complete);

        // TODO: create inventory, implement handler
    }

    @Override
    protected void onEvent(InventoryClickEvent event) {}
}
