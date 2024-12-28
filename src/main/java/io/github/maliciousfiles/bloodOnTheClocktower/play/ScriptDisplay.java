package io.github.maliciousfiles.bloodOnTheClocktower.play;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Role;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.RoleInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.ScriptInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.util.BOTCConfiguration;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.WritableBookContent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
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
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ScriptDisplay implements Listener {
    private static final NamespacedKey BOTC_BOOK = new NamespacedKey(BloodOnTheClocktower.instance, "botc_book");

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

    private static final ItemStack TOWNSFOLK_FILLER = create(Material.LIGHT_BLUE_STAINED_GLASS_PANE, meta ->
            meta.displayName(Component.text("Townsfolk", RoleInfo.ROLE_COLORS.get(Role.Type.TOWNSFOLK))
                    .decoration(TextDecoration.ITALIC, false)));
    private static final ItemStack OUTSIDER_FILLER = create(Material.BLUE_STAINED_GLASS_PANE, meta ->
            meta.displayName(Component.text("Outsider", RoleInfo.ROLE_COLORS.get(Role.Type.OUTSIDER))
                    .decoration(TextDecoration.ITALIC, false)));
    private static final ItemStack MINION_FILLER = create(Material.PINK_STAINED_GLASS_PANE, meta ->
            meta.displayName(Component.text("Minion", RoleInfo.ROLE_COLORS.get(Role.Type.MINION))
                    .decoration(TextDecoration.ITALIC, false)));
    private static final ItemStack DEMON_FILLER = create(Material.RED_STAINED_GLASS_PANE, meta ->
            meta.displayName(Component.text("Demon", RoleInfo.ROLE_COLORS.get(Role.Type.DEMON))
                    .decoration(TextDecoration.ITALIC, false)));
    private static final ItemStack TRAVELLER_FILLER = create(Material.PURPLE_STAINED_GLASS_PANE, meta ->
            meta.displayName(Component.text("Trav", RoleInfo.ROLE_COLORS.get(Role.Type.TOWNSFOLK))
                    .append(Component.text("eller", RoleInfo.ROLE_COLORS.get(Role.Type.MINION)))
                    .decoration(TextDecoration.ITALIC, false)));

    private static final ItemStack EMPTY = create(Material.GRAY_STAINED_GLASS_PANE, meta -> {
        meta.displayName(Component.text("Empty")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GRAY));
        meta.lore(List.of(
                Component.text("Click the feather to create a new script")
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.DARK_GRAY)));
    });

    private static final ItemStack BACK = create(Material.ARROW, meta ->
            meta.displayName(Component.text("Back")
                    .decoration(TextDecoration.ITALIC, false)));
    private static final ItemStack FORWARD = create(Material.ARROW, meta ->
            meta.displayName(Component.text("Forward")
                    .decoration(TextDecoration.ITALIC, false)));

    private static final ItemStack DELETE = create(Material.LAVA_BUCKET, meta -> {
        meta.displayName(Component.text("Delete Script")
                .color(TextColor.color(214, 77, 84))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Delete this script")
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GRAY)));
    });
    private static final ItemStack VIEW = create(Material.SPYGLASS, meta -> {
        meta.displayName(Component.text("View Script")
                .color(TextColor.color(214, 190, 124))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("View the full script")
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GRAY)));
    });
    private static final ItemStack EDIT = create(Material.WRITABLE_BOOK, meta -> {
        meta.displayName(Component.text("Edit Script")
                .color(TextColor.color(117, 159, 214))
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Edit the script JSON")
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GRAY)));
    });

    private static final ItemStack NEW = create(Material.FEATHER, meta -> {
        meta.displayName(Component.text("New Script")
                .color(TextColor.color(96, 214, 197))
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
    private static final ItemStack CONTINUE_ENABLED = create(Material.LIME_CONCRETE, meta -> {
        meta.displayName(Component.text("Continue")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
    });
    private static final ItemStack RETURN = create(Material.BARRIER, meta -> {
        meta.displayName(Component.text("Return")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
    });

    // P P P P P P P P P
    // . . . . . . . . .
    // P P P P P P P P P
    // . . . . . . . . .
    // < . . n s . . . >

    // . . . . . . . . .
    // . . . . . . . . .
    // . . . . . . . . .
    // . . . . . . . . .
    // . . . . . . . . .
    // . . . . . . . . .

    private final CompletableFuture<ScriptInfo> result;
    private final Player player;
    private final int numPages;
    private List<String> scripts = getScripts();

    private int selected = -1;

    private Inventory inventory;
    private int page = 0;
    private ItemStack previouslyHeldItem;
    private boolean saveAsNew;

    private ScriptDisplay(Player player, Inventory inventory, CompletableFuture<ScriptInfo> result) {
        this.result = result;
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

        if (selected != -1) {
            ItemMeta meta = CONTINUE_ENABLED.getItemMeta();
            meta.lore(List.of(Component.text("Selected script: "+scripts.get(selected))
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.GRAY)));
            CONTINUE_ENABLED.setItemMeta(meta);
            contents[40] = CONTINUE_ENABLED;

            int itemIdx = (selected%9)+18*(selected/9) + 8;

            // handle edge cases (literally)
            if (itemIdx % 9 == 8) {
                if (selected % 9 == 8) itemIdx--;
                else itemIdx++;
            }

            contents[itemIdx++] = DELETE;
            contents[itemIdx++] = VIEW;
            contents[itemIdx] = EDIT;
        } else {
            contents[40] = CONTINUE_DISABLED;
        }
        if (page < numPages-1) contents[44] = FORWARD;

        for (int i = 0; i < 18; i++) {
            int index = page * 18 + i;

            int itemIdx = (i%9)+18*(i/9);

            if (index >= scripts.size()) {
                contents[itemIdx] = EMPTY;
                continue;
            }

            String script = scripts.get(index);
            contents[itemIdx] = create(Material.BUNDLE, meta ->{
                meta.displayName(Component.text(script)
                        .decoration(TextDecoration.ITALIC, false));
                if (selected == index) meta.setEnchantmentGlintOverride(true);

                //noinspection UnstableApiUsage
                ((BundleMeta) meta).setItems(((ScriptInfo) config.get(script)).roles.stream().map(RoleInfo::getItem).toList());
            });
        }

        inventory.setContents(contents);
    }

    private void renderViewScript() {
        player.openInventory(inventory = Bukkit.createInventory(null, 54, Component.text("Script Display")));

        ItemStack[] contents = inventory.getContents();

        Map<Role.Type, List<ItemStack>> roles = new HashMap<>();
        for (RoleInfo role : ((ScriptInfo) config.get(scripts.get(selected))).roles) {
            roles.computeIfAbsent(role.type(), k -> new ArrayList<>()).add(role.getItem());
        }

        int midpoint = roles.get(Role.Type.TOWNSFOLK).size()/2+1;
        for (int i = 0; i < 9; i++) {
            contents[i] = i < midpoint ? roles.get(Role.Type.TOWNSFOLK).get(i) : TOWNSFOLK_FILLER;
        }
        for (int i = 0; i < 9; i++) {
            contents[i+9] = i+midpoint < roles.get(Role.Type.TOWNSFOLK).size() ? roles.get(Role.Type.TOWNSFOLK).get(i+midpoint) : TOWNSFOLK_FILLER;
        }

        for (int i = 0; i < 9; i++) {
            contents[i+18] = i < roles.get(Role.Type.OUTSIDER).size() ? roles.get(Role.Type.OUTSIDER).get(i) : OUTSIDER_FILLER;
        }

        for (int i = 0; i < 9; i++) {
            contents[i+27] = i < roles.get(Role.Type.MINION).size() ? roles.get(Role.Type.MINION).get(i) : MINION_FILLER;
        }

        for (int i = 0; i < 9; i++) {
            contents[i+36] = i < roles.get(Role.Type.DEMON).size() ? roles.get(Role.Type.DEMON).get(i) : DEMON_FILLER;
        }

        for (int i = 0; i < 8; i++) {
            contents[i+45] = i < roles.get(Role.Type.TRAVELLER).size() ? roles.get(Role.Type.TRAVELLER).get(i) : TRAVELLER_FILLER;
        }

        contents[53] = RETURN;

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
        if (NEW.equals(evt.getCurrentItem())) {
            selected = -1;
            newScript();
        } else if (CONTINUE_ENABLED.equals(evt.getCurrentItem())) {
            finish();
        } else if (BACK.equals(evt.getCurrentItem())) {
            page--;
            renderPage();
        } else if (FORWARD.equals(evt.getCurrentItem())) {
            page++;
            renderPage();
        } else if (DELETE.equals(evt.getCurrentItem())) {
            config.set(scripts.get(selected), null);
            config.save();

            scripts = getScripts();
            selected = -1;
            renderPage();
        } else if (EDIT.equals(evt.getCurrentItem())) {
            editScript();
            selected = -1;
        } else if (VIEW.equals(evt.getCurrentItem())) {
            renderViewScript();
        } else if (RETURN.equals(evt.getCurrentItem())) {
            player.openInventory(inventory = Bukkit.createInventory(null, 45, Component.text("Script Display")));
            renderPage();
        } else if (inventory.getSize() == 45 && !EMPTY.equals(evt.getCurrentItem()) && (evt.getSlot() < 9 || (evt.getSlot() >= 18 && evt.getSlot() < 27))) {
            int idx = page * 18 + (evt.getSlot()/18)*9 + (evt.getSlot()%9);
            selected = selected == idx ? -1 : idx;
            renderPage();
        }
    }

    private void finish() {
        inventory.close();
        HandlerList.unregisterAll(this);

        result.complete((ScriptInfo) config.get(scripts.get(selected)));
    }

    private void editScript() {
        player.closeInventory();
        player.sendMessage(Component.text("\n\n=============================================", NamedTextColor.GRAY)
                .append(Component.text("\n 1. ", NamedTextColor.WHITE))
                .append(Component.text("Edit the book's JSON, or copy into a file and import into the ", NamedTextColor.GRAY))
                .append(Component.text("online script tool", NamedTextColor.BLUE, TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://script.bloodontheclocktower.com/")))
                .append(Component.text("\n 2. ", NamedTextColor.WHITE))
                .append(Component.text("Replace the JSON in the book with the updated script", NamedTextColor.GRAY))
                .append(Component.text("\n 3. ", NamedTextColor.WHITE))
                .append(Component.text("Click 'Done' to confirm, or throw out the book to cancel", NamedTextColor.GRAY))
                .append(Component.text("\n=============================================", NamedTextColor.GRAY)));

        previouslyHeldItem = player.getInventory().getItem(EquipmentSlot.HAND);

        ItemStack book = ItemStack.of(Material.WRITABLE_BOOK);

        ScriptInfo info = (ScriptInfo) config.get(scripts.get(selected));
        String json = "[{\"author\":\""+info.author+"\",\"name\":\""+info.name+"\"},"+String.join(",", info.roles.stream().map(s->"\""+s.id()+"\"").toList())+"]";

        List<String> pages = new ArrayList<>();
        int i = 0;
        while (i < json.length()) {
            int endIdx = i+200;
            if (endIdx < json.length()) {
                while (endIdx > i && json.charAt(endIdx-1) != ',') endIdx--;
            } else {
                endIdx = json.length();
            }

            pages.add(json.substring(i, endIdx));
            i = endIdx;
        }
        //noinspection UnstableApiUsage
        book.setData(DataComponentTypes.WRITABLE_BOOK_CONTENT, WritableBookContent.writeableBookContent().addPages(pages).build());
        ItemMeta meta = book.getItemMeta();
        meta.getPersistentDataContainer().set(BOTC_BOOK, PersistentDataType.BOOLEAN, true);
        book.setItemMeta(meta);

        player.getInventory().setItem(EquipmentSlot.HAND, book);
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

        ItemStack book = ItemStack.of(Material.WRITABLE_BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.getPersistentDataContainer().set(BOTC_BOOK, PersistentDataType.BOOLEAN, true);
        book.setItemMeta(meta);

        player.getInventory().setItem(EquipmentSlot.HAND, book);
        saveAsNew = true;
    }

    private void finishScript(String script) {
        if (script != null) {
            try {
                List<JsonElement> obj = JsonParser.parseString(script).getAsJsonArray().asList();

                String name = "My Script";
                String author = player.getName();
                if (!obj.getFirst().isJsonPrimitive()) {
                    String givenName = obj.getFirst().getAsJsonObject().get("name").getAsString();
                    String givenAuthor = obj.getFirst().getAsJsonObject().get("author").getAsString();
                    if (!givenName.isEmpty()) name = givenName;
                    if (!givenAuthor.isEmpty()) author = givenAuthor;

                    obj.removeFirst();
                }

                if (saveAsNew) {
                    String origName = name;
                    for (int i = 0; scripts.contains(name = origName + (i == 0 ? "" : " (" + i + ")")); i++) ;
                }

                if (obj.isEmpty()) {
                    player.sendMessage(Component.text("No roles in script, try again", NamedTextColor.RED));
                    return;
                }
                List<String> roleIds = obj.stream().map(JsonElement::getAsString).toList();

                List<RoleInfo> roles = new ArrayList<>();
                for (String id : roleIds) {
                    try {
                        roles.add(RoleInfo.valueOf(id.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(Component.text("Role ID '"+id+"' has not been implemented, try again", NamedTextColor.RED));
                        return;
                    }
                }

                config.set(name, new ScriptInfo(name, author, roles));
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

        Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, () -> player.getInventory().setItem(EquipmentSlot.HAND, previouslyHeldItem));
    }

    @EventHandler
    public void onToss(PlayerDropItemEvent evt) {
        if (!player.equals(evt.getPlayer()) || !Optional.ofNullable(evt.getItemDrop().getItemStack().getItemMeta()).map(m->m.getPersistentDataContainer().has(BOTC_BOOK)).orElse(false)) return;

        evt.getItemDrop().remove();
        finishScript(null);
    }

    @EventHandler
    public void onBook(PlayerEditBookEvent evt) {
        if (!player.equals(evt.getPlayer())) return;

        finishScript(String.join("", evt.getNewBookMeta().getPages()));
    }

    public static void open(Player player, CompletableFuture<ScriptInfo> result) {
        Inventory inventory = Bukkit.createInventory(null, 45, Component.text("Script Display"));

        ScriptDisplay sd = new ScriptDisplay(player, inventory, result);
        Bukkit.getPluginManager().registerEvents(sd, BloodOnTheClocktower.instance);
        sd.renderPage();

        player.openInventory(inventory);
    }
}
