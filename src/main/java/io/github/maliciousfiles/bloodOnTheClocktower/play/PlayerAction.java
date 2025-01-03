package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class PlayerAction implements Listener {
    private final ItemStack enabled, disabled;
    private final int slot;
    private final Player player;

    private Runnable onUse;

    public PlayerAction(Player player, String name, TextColor color, String description, String model, int slot) {
        this.player = player;
        this.enabled = createItem(Material.PAPER,
                DataComponentPair.name(Component.text(name, color, TextDecoration.BOLD)),
                DataComponentPair.lore(Component.text(description, NamedTextColor.GRAY)),
                DataComponentPair.model(model),
                DataComponentPair.cmd(true));
        this.disabled = createItem(Material.PAPER,
                DataComponentPair.name(Component.text(name, NamedTextColor.GRAY, TextDecoration.BOLD)),
                DataComponentPair.lore(Component.text(description, NamedTextColor.DARK_GRAY)),
                DataComponentPair.model(model),
                DataComponentPair.cmd(false));
        this.slot = slot;

        disable();
        Bukkit.getPluginManager().registerEvents(this, BloodOnTheClocktower.instance);
    }

    public PlayerAction(Player player, String name, TextColor color, String description, Material material, int slot) {
        this.player = player;
        this.enabled = this.disabled = createItem(material,
                DataComponentPair.name(Component.text(name, color, TextDecoration.BOLD)),
                DataComponentPair.lore(Component.text(description, NamedTextColor.GRAY)));
        this.slot = slot;

        disable();
        Bukkit.getPluginManager().registerEvents(this, BloodOnTheClocktower.instance);
    }

    public boolean isItem(ItemStack item) {
        return enabled.equals(item) || disabled.equals(item);
    }
    public void enable(Runnable onUse) {
        this.onUse = onUse;
        player.getInventory().setItem(slot, enabled);
    }
    public void disable() {
        onUse = null;
        player.getInventory().setItem(slot, disabled);
    }
    public void remove() {
        onUse = null;
        player.getInventory().setItem(slot, null);
    }

    @EventHandler
    public void rightClick(PlayerInteractEvent evt) {
        if (!player.equals(evt.getPlayer())
                || !isItem(evt.getItem())
                || !evt.getAction().isRightClick()) return;
        evt.setCancelled(true);

        if (enabled.equals(evt.getItem()) && onUse != null) onUse.run();
    }
}
