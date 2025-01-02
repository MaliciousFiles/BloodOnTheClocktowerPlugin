package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.util.CustomPayloadEvent;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Pair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class Grimoire implements Listener {
    public enum Access { STORYTELLER, PLAYER_SELECT, SPY }

    private static final ItemStack FILLER = createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text(" ")));
    private static final ItemStack EMPTY = createItem(Material.GRAY_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("Empty", NamedTextColor.GRAY)),
            DataComponentPair.lore(Component.text("No one sits here", NamedTextColor.DARK_GRAY)));

    private static final NamespacedKey IDX = new NamespacedKey(BloodOnTheClocktower.instance, "grimoire_idx");

    private final Access access;
    private final PlayerWrapper player;
    private final Game game;
    private final Inventory inventory;
    private final List<BOTCPlayer> seatOrder;
    private Grimoire(PlayerWrapper player, Game game, Access access, Inventory inventory) {
        this.access = access;
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
            return createItem(Material.BUNDLE,
                    DataComponentPair.name(Component.text((owner.getName()+"'s Reminder Tokens").replace("s's", "s'"))),
                    DataComponentPair.of(DataComponentTypes.BUNDLE_CONTENTS, BundleContents.bundleContents(owner.reminderTokensOnMe.stream().map(ReminderToken::getItem).toList())),
                    DataComponentPair.custom(Pair.of(IDX, IntTag.valueOf(i))));
        } else if (selected != -1) {
            BOTCPlayer selectedPlayer = seatOrder.get(selected);
            List<ItemStack> tokens = owner.reminderTokensOnMe.stream().filter(t -> t.source == selectedPlayer).map(ReminderToken::getItem).toList();
            if (!tokens.isEmpty()) {
                if (tokens.size() == 1) {
                    return tokens.getFirst();
                } else {
                    return createItem(Material.BUNDLE,
                            DataComponentPair.name(Component.text((owner.getName()+"'s Reminder Tokens From "+selectedPlayer.getName()).replace("s's", "s'"))),
                            DataComponentPair.of(DataComponentTypes.BUNDLE_CONTENTS, BundleContents.bundleContents(tokens)));
                }
            }
        }
        return createItem(Material.PLAYER_HEAD,
                DataComponentPair.name(Component.text(owner.getName())),
                DataComponentPair.lore(Component.text(access == Access.PLAYER_SELECT
                        ? "Click to select player"
                        : "Click to view reminder tokens", NamedTextColor.GRAY)),
                DataComponentPair.of(DataComponentTypes.PROFILE,
                        ResolvableProfile.resolvableProfile(owner.getPlayer().getPlayerProfile())),
                DataComponentPair.custom(Pair.of(IDX, IntTag.valueOf(i))));
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

        int idx = Optional.ofNullable(evt.getCurrentItem()).map(i->DataComponentPair.<IntTag>getCustomData(i, IDX)).map(IntTag::getAsInt).orElse(-1);
        if (idx == -1) return;
        if (access == Access.PLAYER_SELECT) {
            new CustomPayloadEvent(seatOrder.get(idx)).callEvent();
            HandlerList.unregisterAll(this);
            inventory.close();
            return;
        }

        if (selected == idx) selected = -1;
        else selected = idx;
        render();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        if (!inventory.equals(evt.getInventory())) return;
        Bukkit.getScheduler().runTaskLater(BloodOnTheClocktower.instance, () -> {
            if (!inventory.equals(evt.getPlayer().getOpenInventory().getTopInventory())) HandlerList.unregisterAll(this);
        }, 2);
    }

    private static final NamespacedKey GAME_ID = new NamespacedKey(BloodOnTheClocktower.instance, "game_id");
    private static final ItemStack GRIMOIRE = createItem(Material.PAPER,
            DataComponentPair.model("grimoire"),
            DataComponentPair.name(Component.text("Grimoire", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)),
            DataComponentPair.lore(Component.text("Right-click to open the Storyteller's Grimoire", NamedTextColor.GRAY)));

    public static ItemStack createGrimoire(Game game) {
        return DataComponentPair.custom(Pair.of(GAME_ID, StringTag.valueOf(game.getId().toString()))).apply(GRIMOIRE.clone());
    }
    public static Inventory openInventory(Game game, Player player, Access access, Component title) {
        PlayerWrapper botcPlayer = game.getBOTCPlayer(player);
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        Grimoire grimoire = new Grimoire(botcPlayer, game, access, inventory);
        grimoire.render();
        Bukkit.getPluginManager().registerEvents(grimoire, BloodOnTheClocktower.instance);
        player.openInventory(inventory);

        return inventory;
    }
    public static void register() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onRightClick(PlayerInteractEvent evt) {
                if (!evt.getAction().isRightClick() || evt.getItem() == null || evt.getItem().getItemMeta() == null) return;
                String gameId = DataComponentPair.<StringTag>getCustomData(evt.getItem(), GAME_ID).getAsString();
                if (gameId == null) return;

                Game game = Game.getGame(UUID.fromString(gameId));
                if (game == null) { // old grimoire
                    evt.getItem().setAmount(0);
                    return;
                }

                evt.getPlayer().swingHand(evt.getHand());
                openInventory(game, evt.getPlayer(), Access.STORYTELLER, Component.text("Storyteller's Grimoire"));
            }
        }, BloodOnTheClocktower.instance);
    }
}
