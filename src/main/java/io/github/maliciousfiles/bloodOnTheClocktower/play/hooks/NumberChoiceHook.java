package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.stream.IntStream;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class NumberChoiceHook extends MinecraftHook<Integer> {
    private static final ItemStack[] NUMBER_ITEMS = IntStream.range(0, 10)
            .mapToObj(i -> createItem(Material.PAPER,
                    DataComponentPair.name(Component.text("Number " + i, TextColor.color(51, 186, 255))),
                    DataComponentPair.lore(Component.text("Click to select " + i, NamedTextColor.GRAY)),
                    DataComponentPair.model("number"),
                    DataComponentPair.cmd(i)))
            .toArray(ItemStack[]::new);

    private final PlayerWrapper player;
    private final Inventory inventory;
    public NumberChoiceHook(PlayerWrapper player) {
        this.player = player;

        this.inventory = Bukkit.createInventory(null, 9, Component.text("Choose a number", NamedTextColor.DARK_RED));
        inventory.setContents(NUMBER_ITEMS);
        player.getPlayer().openInventory(inventory);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent evt) {
        if (!evt.getPlayer().equals(player.getPlayer()) || !evt.getInventory().equals(inventory)) return;

        Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, () -> player.getPlayer().openInventory(inventory));
    }

    @EventHandler
    public void onClick(InventoryClickEvent evt) {
        if (!evt.getWhoClicked().equals(player.getPlayer()) || !evt.getInventory().equals(inventory)) return;

        evt.setCancelled(true);
        complete(evt.getSlot());
    }

    @Override
    protected void cleanup() {
        Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, inventory::close);
    }
}
