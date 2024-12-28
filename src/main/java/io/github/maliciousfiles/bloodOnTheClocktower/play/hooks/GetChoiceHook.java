package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetChoiceHook<D> extends MinecraftHook<InventoryClickEvent, D> {
    public static class Option {
        private final Object data;
        private final ItemStack item;

        public Option(Object data, Component name, ItemStack icon) {
            this(data, name, icon, List.of());

        }
        public Option(Object data, Component name, ItemStack icon, List<Component> lore) {
            this.data = data;

            this.item = icon;
            ItemMeta meta = this.item.getItemMeta();
            meta.displayName(name);
            meta.lore(lore);
            this.item.setItemMeta(meta);
        }
    }

    public GetChoiceHook(List<Option> options, CompletableFuture<D> complete) {
        super(InventoryClickEvent.class, complete);

        // TODO: create inventory, implement handler
    }

    @Override
    protected void onEvent(InventoryClickEvent event) {}
}
