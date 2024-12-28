package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
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
import java.util.function.Consumer;

public class GrabBag {
    private final boolean allowedToThrow, canTakeMultiple;
    private final List<ItemStack> contents;
    private int index;

    private List<UUID> playersTaken = new ArrayList<>();
    private List<ItemStack> remaining;

    private GrabBag(boolean allowedToThrow, boolean canTakeMultiple, Collection<ItemStack> items) {
        this.allowedToThrow = allowedToThrow;
        this.canTakeMultiple = canTakeMultiple;

        this.contents = new ArrayList<>(items);
        Collections.shuffle(contents);

        this.index = 0;

        this.remaining = new ArrayList<>(items);
    }


    private static final NamespacedKey UUID_KEY = new NamespacedKey(BloodOnTheClocktower.instance, "grab_bag_uuid");
    private static final int PERIOD = 10;

    private static final Map<String, GrabBag> grabBags = new HashMap<>();

    private static String getId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        return item.getItemMeta().getPersistentDataContainer().get(UUID_KEY, PersistentDataType.STRING);
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void checkInventory(Player player, Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            String id = getId(item);
            if (id == null) continue;

            GrabBag bag = grabBags.get(id);
            if (bag == null) {
                item.setAmount(0);
                continue;
            }

            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                items.add(bag.contents.get((bag.index+i) % bag.contents.size()));
            }
            bag.index = bag.index+1 % bag.contents.size();

            item.setData(DataComponentTypes.BUNDLE_CONTENTS, BundleContents.bundleContents(items));
        }
    }

    public static void register() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(BloodOnTheClocktower.instance, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkInventory(player, player.getOpenInventory().getTopInventory());
                checkInventory(player, player.getOpenInventory().getBottomInventory());
            }
        } ,0, PERIOD);

        Bukkit.getPluginManager().registerEvents(new GrabBagHandler(), BloodOnTheClocktower.instance);
    }

    public static ItemStack createGrabBag(Consumer<ItemMeta> setup, boolean allowedToThrow, boolean canTakeMultiple, Collection<ItemStack> items) {
        String uuid = UUID.randomUUID().toString();
        grabBags.put(uuid, new GrabBag(allowedToThrow, canTakeMultiple, items));

        ItemStack ret = ItemStack.of(Material.BUNDLE);
        ItemMeta meta = ret.getItemMeta();
        setup.accept(meta);
        meta.getPersistentDataContainer().set(UUID_KEY, PersistentDataType.STRING, uuid);
        ret.setItemMeta(meta);

        return ret;
    }

    private static ItemStack grab(Player player, GrabBag bag) {
        if (bag.remaining.isEmpty()) return ItemStack.empty();
        if (!bag.canTakeMultiple && bag.playersTaken.contains(player.getUniqueId())) return ItemStack.empty();

        int idx = new Random().nextInt(bag.remaining.size());

        ItemStack ret = bag.remaining.get(idx);
        bag.remaining.remove(idx);
        bag.playersTaken.add(player.getUniqueId());

        return ret;
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

                GrabBag bag = grabBags.get(getId(evt.getItem()));
                if (!bag.allowedToThrow) return;

                ((CraftPlayer) evt.getPlayer()).getHandle()
                        .drop(CraftItemStack.asNMSCopy(grab(evt.getPlayer(), bag)), true);
            }
        }
    }
}
