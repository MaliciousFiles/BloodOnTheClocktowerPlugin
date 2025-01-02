package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Option;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Pair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
import net.minecraft.nbt.StringTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class GrabBag<D> {
    private final List<Option<D>> contents;
    private int index;

    private final List<UUID> playersTaken = new ArrayList<>();
    private final List<Option<D>> remaining;
    private final List<Player> recipients;

    private final Consumer<Pair<Player, D>> onGrab;
    private final CompletableFuture<Map<Player, D>> future;
    private final Map<Player, D> received = new HashMap<>();

    private GrabBag(Collection<Option<D>> options, List<Player> recipients, Consumer<Pair<Player, D>> onGrab, CompletableFuture<Map<Player, D>> future) {
        this.recipients = recipients;
        this.future = future;
        this.onGrab = onGrab;
        this.remaining = new ArrayList<>(options);
        this.contents = new ArrayList<>(options);
        Collections.shuffle(contents);

        this.index = 0;
    }


    private static final NamespacedKey UUID_KEY = new NamespacedKey(BloodOnTheClocktower.instance, "grab_bag_uuid");
    private static final int PERIOD = 10;

    private static final Map<String, GrabBag<?>> grabBags = new HashMap<>();

    private static String getId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        return DataComponentPair.<StringTag>getCustomData(item, UUID_KEY).getAsString();
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void checkInventory(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            String id = getId(item);
            if (id == null) continue;

            GrabBag<?> bag = grabBags.get(id);
            if (bag == null) {
                item.setAmount(0);
                continue;
            }

            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                items.add(bag.contents.get((bag.index+i) % bag.contents.size()).representation());
            }
            bag.index = bag.index+1 % bag.contents.size();

            item.setData(DataComponentTypes.BUNDLE_CONTENTS, BundleContents.bundleContents(items));
        }
    }

    public static void register() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(BloodOnTheClocktower.instance, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkInventory(player.getOpenInventory().getTopInventory());
                checkInventory(player.getOpenInventory().getBottomInventory());
            }
        } ,0, PERIOD);

        Bukkit.getPluginManager().registerEvents(new GrabBagHandler(), BloodOnTheClocktower.instance);
    }

    public static <D> ItemStack createGrabBag(Collection<Option<D>> options, List<Player> recipients, Consumer<Pair<Player, D>> onGrab, CompletableFuture<Map<Player, D>> future, DataComponentPair<?> data) {
        String uuid = UUID.randomUUID().toString();
        grabBags.put(uuid, new GrabBag<>(options, recipients, onGrab, future));

        return createItem(Material.BUNDLE,
                data,
                DataComponentPair.custom(Pair.of(UUID_KEY, StringTag.valueOf(uuid))));
    }

    public static void removeGrabBag(ItemStack grabBag) {
        String id = getId(grabBag);
        if (id == null) return;

        grabBags.remove(id);
    }

    private static <D> ItemStack grab(Player player, GrabBag<D> bag) {
        if (bag.remaining.isEmpty()) return ItemStack.empty();
        if (bag.playersTaken.contains(player.getUniqueId())) return ItemStack.empty();
        if (!bag.recipients.contains(player)) return ItemStack.empty();

        int idx = new Random().nextInt(bag.remaining.size());

        Option<D> ret = bag.remaining.get(idx);
        bag.remaining.remove(idx);
        bag.playersTaken.add(player.getUniqueId());
        bag.received.put(player, ret.data());

        bag.onGrab.accept(Pair.of(player, ret.data()));
        if (bag.remaining.isEmpty()) bag.future.complete(bag.received);

        return ret.representation();
    }

    private static class GrabBagHandler implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent evt) {
            if (evt.getClick() == ClickType.LEFT) {
                if (getId(evt.getCursor()) != null && evt.getCurrentItem() != null) {
                    evt.setCancelled(true);

                    ItemStack cursor = evt.getCursor();
                    evt.setCursor(evt.getCurrentItem());
                    evt.setCurrentItem(cursor);
                } else if (!evt.getCursor().isEmpty() && getId(evt.getCurrentItem()) != null) {
                    evt.setCancelled(true);

                    ItemStack cursor = evt.getCursor();
                    evt.setCursor(evt.getCurrentItem());
                    evt.setCurrentItem(cursor);
                }
            } else if (evt.getClick() == ClickType.RIGHT) {
                if (evt.getCursor().isEmpty() && getId(evt.getCurrentItem()) != null) {
                    evt.setCancelled(true);
                    evt.setCursor(grab((Player) evt.getWhoClicked(), grabBags.get(getId(evt.getCurrentItem()))));
                } if (getId(evt.getCursor()) != null && (evt.getCurrentItem() == null || evt.getCurrentItem().isEmpty())) {
                    evt.setCancelled(true);
                    evt.setCurrentItem(evt.getCursor());
                    evt.setCursor(ItemStack.empty());
                }
            }
        }

        @EventHandler
        public void onUse(PlayerInteractEvent evt) {
            if (evt.getAction().isRightClick() && evt.getItem() != null && getId(evt.getItem()) != null) {
                evt.setCancelled(true);
            }
        }
    }
}
