package io.github.maliciousfiles.bloodOnTheClocktower.play;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Role;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.ScriptInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.util.BOTCConfiguration;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.text.Filtered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class ScriptDisplay implements Listener {
    private static final BOTCConfiguration config = BOTCConfiguration.getConfig("scripts.yml");
    private static List<String> getScripts() {
        return new ArrayList<>(config.getKeys(false).stream().sorted().toList());
    }

    private static ItemStack create(Material material, Consumer<ItemMeta> metaConsumer) {
        ItemStack item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();
        metaConsumer.accept(meta);
        item.setItemMeta(meta);
        return item;
    }

    private static final ItemStack FILLER = create(Material.LIGHT_GRAY_STAINED_GLASS_PANE, meta ->
            meta.displayName(Component.text(" ")));

    private static final ItemStack EMPTY = create(Material.GRAY_STAINED_GLASS_PANE, meta -> {
        meta.displayName(Component.text("Empty")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GRAY));
        meta.lore(List.of(
                Component.text("Click the dead bush to create a new script")
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.DARK_GRAY)));
    });

    private static final ItemStack BACK = create(Material.ARROW, meta ->
            meta.displayName(Component.text("Back")
                    .decoration(TextDecoration.ITALIC, false)));
    private static final ItemStack FORWARD = create(Material.ARROW, meta ->
            meta.displayName(Component.text("Forward")
                    .decoration(TextDecoration.ITALIC, false)));

    private static final ItemStack NEW = create(Material.DEAD_BUSH, meta -> {
        meta.displayName(Component.text("New Script")
                .color(TextColor.color(117, 214, 59))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Create a new script")
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GRAY)));
    });
    private static final ItemStack CONTINUE_DISABLED = create(Material.GRAY_CONCRETE, meta -> {
        meta.displayName(Component.text("Continue")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Select a script to continue")
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.DARK_GRAY)));
    });
    private static final ItemStack CONTINUE_ENABLED = create(Material.GREEN_CONCRETE, meta -> {
        meta.displayName(Component.text("Continue")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Selected script: {}") // TODO: fill in script name
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GRAY)));
    });

    // P P P P P P P P P
    // . . . . . . . . .
    // P P P P P P P P P
    // . . . . . . . . .
    // < . . n s . . . >

    private final Inventory inventory;
    private final Player player;
    private final int numPages;
    private List<String> scripts = getScripts();

    private int page = 0;
    private ItemStack previouslyHeldItem, book;

    private ScriptDisplay(Player player, Inventory inventory) {
        this.inventory = inventory;
        this.player = player;
        numPages = Math.max((int) Math.ceil(scripts.size() / 18f), 1);
    }

    private void renderPage() {
        inventory.clear();

        ItemStack[] contents = inventory.getContents();
        Arrays.fill(contents, FILLER);

        if (page > 0) inventory.setItem(36, BACK);
        contents[39] = NEW;
        contents[40] = CONTINUE_DISABLED;
        if (page < numPages-1) contents[44] = FORWARD;

        for (int i = 0; i < 18; i++) {
            int index = page * 18 + i;

            if (index >= scripts.size()) {
                contents[(i%9)+18*(i/9)] = EMPTY;
                continue;
            }

            String script = scripts.get(index);
            contents[i] = create(Material.BUNDLE, meta ->{
                meta.displayName(Component.text(script)
                        .decoration(TextDecoration.ITALIC, false));

                ScriptInfo info = (ScriptInfo) config.get(script);
                List<ItemStack> items = new ArrayList<>();

                for (String roleId : info.roleIds) {
                    Role role = null;
                    try {
                        role = Role.BY_ID.get(roleId).getConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }

                    ItemStack item = ItemStack.of(Material.PAPER);
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.displayName(Component.text(role.getRoleName())
                            .decoration(TextDecoration.ITALIC, false));
                    itemMeta.lore(List.of(
                            Component.text(role.getRoleDescription())
                                    .decoration(TextDecoration.ITALIC, false)
                                    .color(NamedTextColor.GRAY)));
                    itemMeta.setCustomModelData(Role.CMD_IDs.get(roleId));
                    item.setItemMeta(itemMeta);
                    items.add(item);
                }

                //noinspection UnstableApiUsage
                ((BundleMeta) meta).setItems(items);
            });
        }

        inventory.setContents(contents);
    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent evt) {
        if (!inventory.equals(evt.getInventory()) || evt.getReason() != InventoryCloseEvent.Reason.PLAYER) return;

        Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, () -> evt.getPlayer().openInventory(inventory));
    }

    @EventHandler
    public void inventoryHandler(InventoryClickEvent evt) {
        if (!inventory.equals(evt.getClickedInventory())) return;

        evt.setCancelled(true);
        if (NEW.equals(evt.getCurrentItem())) newScript();
        else if (CONTINUE_ENABLED.equals(evt.getCurrentItem())) continueSelection();
        else if (BACK.equals(evt.getCurrentItem())) {
            page--;
            renderPage();
        } else if (FORWARD.equals(evt.getCurrentItem())) {
            page++;
            renderPage();
        }
    }

    private void continueSelection() {

    }

    private void newScript() {
        player.closeInventory();
        player.sendMessage(Component.text("\n\n=============================================", NamedTextColor.GRAY)
                .append(Component.text("\n 1. ", NamedTextColor.WHITE))
                .append(Component.text("Build a script in the ", NamedTextColor.GRAY))
                .append(Component.text("online script tool", NamedTextColor.BLUE, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://script.bloodontheclocktower.com/")))
                .append(Component.text("\n 2. ", NamedTextColor.WHITE))
                .append(Component.text("Save your script to clipboard", NamedTextColor.GRAY))
                .append(Component.text("\n 3. ", NamedTextColor.WHITE))
                .append(Component.text("Paste your script into the provided book and quill", NamedTextColor.GRAY))
                .append(Component.text("\n    (If necessary, split onto multiple pages)", NamedTextColor.DARK_GRAY))
                .append(Component.text("\n 4. ", NamedTextColor.WHITE))
                .append(Component.text("Click 'Done' to confirm, or throw out the book to cancel", NamedTextColor.GRAY))
                .append(Component.text("\n=============================================", NamedTextColor.GRAY)));

        previouslyHeldItem = player.getInventory().getItem(EquipmentSlot.HAND);
        player.getInventory().setItem(EquipmentSlot.HAND, book = ItemStack.of(Material.WRITABLE_BOOK));
    }

    private void finishNewScript(String script) {
        player.getInventory().setItem(EquipmentSlot.HAND, previouslyHeldItem);

        if (script != null) {
            try {
                List<JsonElement> obj = JsonParser.parseString(script).getAsJsonArray().asList();

                String name = obj.getFirst().getAsJsonObject().get("name").getAsString();
                String author = obj.getFirst().getAsJsonObject().get("author").getAsString();
                if (name.isEmpty()) name = "My Script";
                if (author.isEmpty()) author = player.getName();

                obj.removeFirst();

                if (obj.isEmpty()) {
                    player.sendMessage(Component.text("No roles in script, try again", NamedTextColor.RED));
                    return;
                }
                List<String> roleIds = obj.stream().map(JsonElement::getAsString).toList();

                for (String id : roleIds) {
                    if (Role.BY_ID.containsKey(id)) continue;

                    player.sendMessage(Component.text("Role ID '"+id+"' has not been implemented, try again", NamedTextColor.RED));
                    return;
                }

                config.set(name, new ScriptInfo(name, author, roleIds));
                config.save();

                scripts = getScripts();
            } catch (JsonParseException | UnsupportedOperationException | IllegalStateException | NullPointerException | NoSuchElementException e) {
                player.sendMessage(Component.text("Invalid script format, try again", NamedTextColor.RED)
                        .append(Component.text("\n(make sure you copy as JSON from the script tool)", NamedTextColor.DARK_RED)));
                return;
            }
        }

        renderPage();
        player.openInventory(inventory);
    }

    @EventHandler
    public void onToss(PlayerDropItemEvent evt) {
        if (!player.equals(evt.getPlayer()) || !book.equals(evt.getItemDrop().getItemStack())) return;

        evt.getItemDrop().remove();
        finishNewScript(null);
    }

    @EventHandler
    public void onBook(PlayerEditBookEvent evt) {
        if (!player.equals(evt.getPlayer())) return;

        finishNewScript(String.join("", evt.getNewBookMeta().getPages()));
    }

    public static void open(Player player) {
        Inventory inventory = player.getServer().createInventory(player, 45, Component.text("Script Display"));

        ScriptDisplay sd = new ScriptDisplay(player, inventory);
        Bukkit.getPluginManager().registerEvents(sd, BloodOnTheClocktower.instance);
        sd.renderPage();

        player.openInventory(inventory);
    }
}
