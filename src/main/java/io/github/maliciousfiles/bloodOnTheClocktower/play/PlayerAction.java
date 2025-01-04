package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class PlayerAction implements Listener {
    private final ItemStack enabled, disabled;
    private final Component name;
    private final TextColor color;
    private final int slot;
    private final Player player;

    private final Stack<Runnable> onUse = new Stack<>();
    private final Stack<Component> names = new Stack<>();

    public PlayerAction(Player player, String name, TextColor color, String description, String model, int slot) {
        this(player, name, color, description, BloodOnTheClocktower.key(model), slot);
    }
    public PlayerAction(Player player, String name, TextColor color, String description, NamespacedKey model, int slot) {
        this.player = player;
        this.enabled = createItem(Material.PAPER,
                DataComponentPair.name(this.name = Component.text(name, this.color = color, TextDecoration.BOLD)),
                DataComponentPair.lore(Component.text(description, NamedTextColor.GRAY)),
                DataComponentPair.of(DataComponentTypes.ITEM_MODEL, model),
                DataComponentPair.cmd(true));
        this.disabled = createItem(Material.PAPER,
                DataComponentPair.name(Component.text(name, NamedTextColor.GRAY, TextDecoration.BOLD)),
                DataComponentPair.lore(Component.text(description, NamedTextColor.DARK_GRAY)),
                DataComponentPair.of(DataComponentTypes.ITEM_MODEL, model),
                DataComponentPair.cmd(false));
        this.slot = slot;

        Bukkit.getPluginManager().registerEvents(this, BloodOnTheClocktower.instance);
    }

    public TextColor color() {
        return color;
    }

    public boolean isItem(ItemStack item) {
        return enabled.equals(item) || disabled.equals(item);
    }

    public void enable(Runnable onUse, Component name) {
        if (onUse != null) {
            this.onUse.push(onUse);
            this.names.push(name.color(color));
        }
        DataComponentPair.name(name.color(color)).apply(enabled);
        player.getInventory().setItem(slot, enabled);
    }
    public void enable(Runnable onUse) {
        enable(onUse, name);
    }
    public void disable() {
        if (!onUse.empty()) {
            onUse.pop();
            names.pop();
        }
        if (onUse.empty()) {
            player.getInventory().setItem(slot, disabled);
        } else {
            player.getInventory().setItem(slot, DataComponentPair.name(names.peek()).apply(enabled));
        }
    }
    public void tempDisable() {
        player.getInventory().setItem(slot, disabled);
    }

    public void remove() {
        onUse.clear();
        player.getInventory().setItem(slot, null);
    }

    @EventHandler
    public void rightClick(PlayerInteractEvent evt) {
        if (!player.equals(evt.getPlayer())
                || !isItem(evt.getItem())
                || !evt.getAction().isRightClick()) return;
        evt.setCancelled(true);

        if (enabled.equals(evt.getItem()) && !onUse.empty()) onUse.peek().run();
    }

    @EventHandler
    public void onToss(PlayerDropItemEvent evt) {
        if (isItem(evt.getItemDrop().getItemStack())) evt.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (isItem(evt.getCurrentItem())) evt.setCancelled(true);
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent evt) {
        if (isItem(evt.getOffHandItem())) evt.setCancelled(true);
    }
}
