package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Stream;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class Grimoire implements Listener {
    private static final ItemStack FILLER = createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, meta ->
            meta.displayName(Component.text(" ")));
    private static final ItemStack EMPTY = createItem(Material.GRAY_STAINED_GLASS_PANE, meta -> {
        meta.displayName(Component.text("Empty", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("No one sits here", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false)));
    });

    private static final NamespacedKey IDX = new NamespacedKey(BloodOnTheClocktower.instance, "botc_grimoire_idx");

    private final PlayerWrapper player;
    private final Game game;
    private final Inventory inventory;
    private final List<BOTCPlayer> seatOrder;
    private Grimoire(PlayerWrapper player, Game game, Inventory inventory) {
        this.player = player;
        this.game = game;
        this.inventory = inventory;

        seatOrder = new ArrayList<>(game.getSeats().getSeatOrder().stream().map(p->p == null ? null : game.getBOTCPlayer(p)).toList());
        if (seatOrder.getLast() != null && seatOrder.stream().anyMatch(Objects::isNull)) {
            for (int i = seatOrder.lastIndexOf(null)+1; i < seatOrder.size(); i++) {
                seatOrder.set(i, i == seatOrder.size()-1 ? null : seatOrder.get(i+1));
            }
        }
    }

    private int selected = -1;

    private ItemStack createRole(int i) {
        BOTCPlayer owner = seatOrder.get(i);
        if (owner == null) return EMPTY;

        ItemStack item = owner.getRole().info.getItem();
        ItemMeta meta = item.getItemMeta();
        if (selected == i) meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createHead(int i) {
        BOTCPlayer owner = seatOrder.get(i);
        if (owner == null) return FILLER;

        if (selected == i) {
            return createItem(Material.BUNDLE, meta -> {
                meta.displayName(Component.text((owner.getName()+"'s Reminder Tokens").replace("s's", "s'"))
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(List.of(Component.text("Click to deselect", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)));
                meta.getPersistentDataContainer().set(IDX, PersistentDataType.INTEGER, i);
                //noinspection UnstableApiUsage
                ((BundleMeta) meta).setItems(owner.reminderTokensOnMe.stream()
                        .map(ReminderToken::getItem).toList());
            });
        } else if (selected != -1) {
            BOTCPlayer selectedPlayer = seatOrder.get(selected);
            List<ItemStack> tokens = owner.reminderTokensOnMe.stream().filter(t -> t.source == selectedPlayer).map(ReminderToken::getItem).toList();
            if (!tokens.isEmpty()) {
                if (tokens.size() == 1) {
                    return tokens.getFirst();
                } else {
                    return createItem(Material.BUNDLE, meta -> {
                        meta.displayName(Component.text((owner.getName()+"'s Reminder Tokens From "+selectedPlayer.getName()).replace("s's", "s'"))
                                .decoration(TextDecoration.ITALIC, false));
                        //noinspection UnstableApiUsage
                        ((BundleMeta) meta).setItems(tokens);
                    });
                }
            }
        }
        return createItem(Material.PLAYER_HEAD, meta -> {
            meta.displayName(Component.text(owner.getName())
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text("Click to view reminder tokens", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)));
            meta.getPersistentDataContainer().set(IDX, PersistentDataType.INTEGER, i);
            ((SkullMeta) meta).setOwningPlayer(owner.getPlayer());
        });
    }

    // ..PPPPP..
    // PRRRRRR..
    // PR.....RP
    // PR.....RP
    // ..RRRRR..
    // ..PPPPP..
    private void render() {
        ItemStack[] contents = inventory.getContents();
        Arrays.fill(contents, FILLER);

        for (int i = 0; i < 5; i++) {
            contents[i+2] = createHead(i);
            contents[i+11] = createRole(i);
        }

        contents[25] = createRole(5);
        contents[26] = createHead(5);

        contents[34] = createRole(6);
        contents[35] = createHead(6);

        for (int i = 0; i < 5; i++) {
            contents[i+38] = createRole(11-i);
            contents[i+47] = createHead(11-i);
        }

        contents[28] = createRole(12);
        contents[27] = createHead(12);

        contents[19] = createRole(13);
        contents[18] = createHead(13);

        if (seatOrder.getLast() != null) {
            contents[10] = createRole(14);
            contents[9] = createHead(14);
        }

        inventory.setContents(contents);
    }

    @EventHandler
    public void inventoryInteract(InventoryClickEvent evt) {
        if (!inventory.equals(evt.getClickedInventory())) return;
        evt.setCancelled(true);

        int idx = Optional.ofNullable(evt.getCurrentItem()).map(i->Optional.ofNullable(i.getItemMeta()).map(m->m.getPersistentDataContainer().getOrDefault(IDX, PersistentDataType.INTEGER, -1)).orElse(-1)).orElse(-1);
        if (idx == -1) return;

        if (selected == idx) selected = -1;
        else selected = idx;
        render();
    }

    private static final NamespacedKey GAME_ID = new NamespacedKey(BloodOnTheClocktower.instance, "botc_game_id");
    private static final ItemStack GRIMOIRE = ((CraftItemType<ItemMeta>) Material.PAPER.asItemType()).createItemStack(meta -> {
        meta.displayName(Component.text("Grimoire")
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Right-click to open the Storyteller's Grimoire", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.setCustomModelData(-1);
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
                if (!evt.getAction().isRightClick() || evt.getItem() == null || evt.getItem().getItemMeta() == null) return;
                String gameId = evt.getItem().getItemMeta().getPersistentDataContainer().get(GAME_ID, PersistentDataType.STRING);
                if (gameId == null) return;

                Game game = Game.getGame(UUID.fromString(gameId));
                PlayerWrapper player = game.getBOTCPlayer(evt.getPlayer());
                Inventory inventory = Bukkit.createInventory(null, 54, Component.text("Storyteller's Grimoire"));

                Grimoire grimoire = new Grimoire(player, game, inventory);
                grimoire.render();
                Bukkit.getPluginManager().registerEvents(grimoire, BloodOnTheClocktower.instance);
                evt.getPlayer().openInventory(inventory);
            }
        }, BloodOnTheClocktower.instance);
    }
}
