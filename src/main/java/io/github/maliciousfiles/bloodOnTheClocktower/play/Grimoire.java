package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class Grimoire implements Listener {
    private final PlayerWrapper player;
    private final Game game;
    private final Inventory inventory;
    private Grimoire(PlayerWrapper player, Game game, Inventory inventory) {
        this.player = player;
        this.game = game;
        this.inventory = inventory;
    }
    // .........
    // .........
    // .........
    // .........
    // .........
    // .........

    private void render() {
        ItemStack[] contents = inventory.getContents();

    }

    private static final NamespacedKey GAME_ID = new NamespacedKey(BloodOnTheClocktower.instance, "botc_game_id");
    private static final ItemStack GRIMOIRE = ((CraftItemType<ItemMeta>) Material.BOOK.asItemType()).createItemStack(meta -> {
        meta.displayName(Component.text("Grimoire")
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Right-click to open the Storyteller's Grimoire", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
    });
    public static ItemStack createGrimoire(Game game) {
        ItemStack item = GRIMOIRE.clone();

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(GAME_ID, PersistentDataType.STRING, game.getId().toString());
        item.setItemMeta(meta);

        return item;
    }
    public static void register() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onRightClick(PlayerInteractEvent evt) {
                if (!evt.getAction().isRightClick() || !GRIMOIRE.equals(evt.getItem())) return;

                Game game = Game.getGame(UUID.fromString(evt.getItem().getItemMeta().getPersistentDataContainer().get(GAME_ID, PersistentDataType.STRING)));
                PlayerWrapper player = game.getBOTCPlayer(evt.getPlayer());
                Inventory inventory = Bukkit.createInventory(null, 54, Component.text("Storyteller's Grimoire"));

                Grimoire grimoire = new Grimoire(player, game, inventory);
                grimoire.render();
                evt.getPlayer().openInventory(inventory);
            }
        }, BloodOnTheClocktower.instance);
    }
}
