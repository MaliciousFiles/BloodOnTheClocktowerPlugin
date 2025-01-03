package io.github.maliciousfiles.bloodOnTheClocktower.play;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Role;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.RoleInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.ScriptInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.util.BOTCConfiguration;
import io.github.maliciousfiles.bloodOnTheClocktower.util.CustomPayloadEvent;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Pair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
import io.papermc.paper.datacomponent.item.WritableBookContent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.ByteTag;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class ScriptDisplay implements Listener {
    private static final NamespacedKey BOTC_BOOK = BloodOnTheClocktower.key("book");

    private static final BOTCConfiguration config = BOTCConfiguration.getConfig("scripts.yml");
    private static List<String> getScripts() {
        return new ArrayList<>(config.getKeys(false).stream().sorted().toList());
    }

    private static final ItemStack FILLER = createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text(" ", NamedTextColor.GRAY)));

    private static final ItemStack TOWNSFOLK_FILLER = createItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("Townsfolk", RoleInfo.ROLE_COLORS.get(Role.Type.TOWNSFOLK))));
    private static final ItemStack OUTSIDER_FILLER = createItem(Material.BLUE_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("Outsider", RoleInfo.ROLE_COLORS.get(Role.Type.OUTSIDER))));
    private static final ItemStack MINION_FILLER = createItem(Material.PINK_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("Minion", RoleInfo.ROLE_COLORS.get(Role.Type.MINION))));
    private static final ItemStack DEMON_FILLER = createItem(Material.RED_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("Demon", RoleInfo.ROLE_COLORS.get(Role.Type.DEMON))));
    private static final ItemStack TRAVELLER_FILLER = createItem(Material.PURPLE_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("Trav", RoleInfo.ROLE_COLORS.get(Role.Type.TOWNSFOLK))
                    .append(Component.text("eller", RoleInfo.ROLE_COLORS.get(Role.Type.MINION)))));

    private static final ItemStack EMPTY = createItem(Material.GRAY_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("Empty", NamedTextColor.GRAY)),
            DataComponentPair.lore(Component.text("Click the feather to create a new script", NamedTextColor.DARK_GRAY)));

    private static final ItemStack BACK = createItem(Material.ARROW,
            DataComponentPair.name(Component.text("Back")));
    private static final ItemStack FORWARD = createItem(Material.ARROW,
            DataComponentPair.name(Component.text("Forward")));

    private static final ItemStack DELETE = createItem(Material.LAVA_BUCKET,
            DataComponentPair.name(Component.text("Delete Script", TextColor.color(214, 77, 84))),
            DataComponentPair.lore(Component.text("Delete this script", NamedTextColor.GRAY)));
    private static final ItemStack VIEW = createItem(Material.SPYGLASS,
            DataComponentPair.name(Component.text("View Script", TextColor.color(214, 190, 124))),
            DataComponentPair.lore(Component.text("View the full script", NamedTextColor.GRAY)));
    private static final ItemStack EDIT = createItem(Material.WRITABLE_BOOK,
            DataComponentPair.name(Component.text("Edit Script", TextColor.color(117, 159, 214))),
            DataComponentPair.lore(Component.text("Edit the script JSON", NamedTextColor.GRAY)));

    private static final ItemStack NEW = createItem(Material.FEATHER,
            DataComponentPair.name(Component.text("New Script", TextColor.color(96, 214, 197))),
            DataComponentPair.lore(Component.text("Create a new script", NamedTextColor.GRAY)));
    private static final ItemStack CONTINUE_DISABLED = createItem(Material.GRAY_CONCRETE,
            DataComponentPair.name(Component.text("Continue", NamedTextColor.GRAY)),
            DataComponentPair.lore(Component.text("Select a script to continue", NamedTextColor.DARK_GRAY)));
    private static final ItemStack CONTINUE_ENABLED = createItem(Material.LIME_CONCRETE,
            DataComponentPair.name(Component.text("Continue", NamedTextColor.GREEN)));
    private static final ItemStack RETURN = createItem(Material.BARRIER,
            DataComponentPair.name(Component.text("Return", NamedTextColor.RED)));

            // P P P P P P P P P
    // . . . . . . . . .
    // P P P P P P P P P
    // . . . . . . . . .
    // < . . n s . . . >

    private final CompletableFuture<ScriptInfo> scriptFuture;
    private final CompletableFuture<List<RoleInfo>> rolesFuture;
    private final Player player;
    private final int numPages, numPlayers;
    private List<String> scripts = getScripts();

    private int selected = -1;

    private Inventory inventory;
    private int page = 0;
    private ItemStack previouslyHeldItem;
    private boolean saveAsNew;

    private boolean selectingRoles;
    private List<RoleInfo> selectedRoles = new ArrayList<>();

    private ScriptInfo viewingScript;
    private int rolesToSelect;

    private ScriptDisplay(Player player, Inventory inventory, int numPlayers, CompletableFuture<ScriptInfo> script, CompletableFuture<List<RoleInfo>> roles) {
        this.scriptFuture = script;
        this.rolesFuture = roles;
        this.inventory = inventory;
        this.player = player;
        this.numPlayers = numPlayers;
        numPages = Math.max((int) Math.ceil(scripts.size() / 18f), 1);
    }

    private void renderPage() {
        inventory.clear();

        ItemStack[] contents = inventory.getContents();
        Arrays.fill(contents, FILLER);

        if (page > 0) inventory.setItem(36, BACK);
        contents[39] = NEW;

        if (selected != -1) {
            contents[40] = DataComponentPair.lore(
                    Component.text("Selected script: "+scripts.get(selected), NamedTextColor.GRAY))
                    .apply(CONTINUE_ENABLED);

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
            contents[itemIdx] = createItem(Material.BUNDLE,
                    DataComponentPair.name(Component.text(script)),
                    DataComponentPair.of(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, selected == index),
                    DataComponentPair.of(DataComponentTypes.BUNDLE_CONTENTS, BundleContents.bundleContents(((ScriptInfo) config.get(script)).roles.stream().map(RoleInfo::getItem).toList())));
        }

        inventory.setContents(contents);
    }

    private void renderViewScript(Component title) {
        if (title != null) player.openInventory(inventory = Bukkit.createInventory(null, selectingRoles ? 45 : 54, title));

        ItemStack[] contents = inventory.getContents();

        Map<Role.Type, List<RoleInfo>> roles = new HashMap<>();
        for (RoleInfo role : (viewingScript == null ? (ScriptInfo) config.get(scripts.get(selected)) : viewingScript).roles) {
            roles.computeIfAbsent(role.type(), k -> new ArrayList<>()).add(role);
        }

        int midpoint = roles.get(Role.Type.TOWNSFOLK).size()/2+1;
        for (int i = 0; i < 9; i++) {
            RoleInfo role = i < midpoint ? roles.get(Role.Type.TOWNSFOLK).get(i) : null;
            contents[i] = Optional.ofNullable(role).map(RoleInfo::getItem).orElse(TOWNSFOLK_FILLER);
            if (selectedRoles.contains(role)) {
                contents[i].setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }
        for (int i = 0; i < 9; i++) {
            RoleInfo role = i+midpoint < roles.get(Role.Type.TOWNSFOLK).size() ? roles.get(Role.Type.TOWNSFOLK).get(i+midpoint) : null;
            contents[i+9] = Optional.ofNullable(role).map(RoleInfo::getItem).orElse(TOWNSFOLK_FILLER);
            if (selectedRoles.contains(role)) {
                contents[i+9].setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        for (int i = 0; i < 9; i++) {
            RoleInfo role = i < roles.get(Role.Type.OUTSIDER).size() ? roles.get(Role.Type.OUTSIDER).get(i) : null;
            contents[i+18] = Optional.ofNullable(role).map(RoleInfo::getItem).orElse(OUTSIDER_FILLER);
            if (selectedRoles.contains(role)) {
                contents[i+18].setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        for (int i = 0; i < 9; i++) {
            RoleInfo role = i < roles.get(Role.Type.MINION).size() ? roles.get(Role.Type.MINION).get(i) : null;
            contents[i+27] = Optional.ofNullable(role).map(RoleInfo::getItem).orElse(MINION_FILLER);
            if (selectedRoles.contains(role)) {
                contents[i+27].setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        for (int i = 0; i < 9; i++) {
            RoleInfo role = i < roles.get(Role.Type.DEMON).size() ? roles.get(Role.Type.DEMON).get(i) : null;
            contents[i+36] = Optional.ofNullable(role).map(RoleInfo::getItem).orElse(DEMON_FILLER);
            if (selectedRoles.contains(role)) {
                contents[i+36].setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            }
        }

        if (selectingRoles) {
            ItemStack item = CONTINUE_ENABLED;
            Component error = null;
            if (selectedRoles.size() != numPlayers) {
                item = CONTINUE_DISABLED.clone();
                error = Component.text("Must have exactly as many roles as players ("+numPlayers+")", NamedTextColor.RED);
            }

            List<Component> lore = Lists.newArrayList(
                    Component.text("Townsfolk: ", NamedTextColor.GRAY)
                            .append(Component.text(selectedRoles.stream().filter(r->r.type() == Role.Type.TOWNSFOLK).count(), RoleInfo.ROLE_COLORS.get(Role.Type.TOWNSFOLK)))
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Outsiders: ", NamedTextColor.GRAY)
                            .append(Component.text(selectedRoles.stream().filter(r->r.type() == Role.Type.OUTSIDER).count(), RoleInfo.ROLE_COLORS.get(Role.Type.OUTSIDER)))
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Minions: ", NamedTextColor.GRAY)
                            .append(Component.text(selectedRoles.stream().filter(r->r.type() == Role.Type.MINION).count(), RoleInfo.ROLE_COLORS.get(Role.Type.MINION)))
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Demons: ", NamedTextColor.GRAY)
                            .append(Component.text(selectedRoles.stream().filter(r->r.type() == Role.Type.DEMON).count(), RoleInfo.ROLE_COLORS.get(Role.Type.DEMON)))
                            .decoration(TextDecoration.ITALIC, false)
            );
            if (error != null) lore.addAll(List.of(Component.empty(), error));

            contents[44] = DataComponentPair.lore(lore.toArray(Component[]::new)).apply(item);
            contents[43] = createItem(Material.PAPER,
                    DataComponentPair.name(Component.text("Recommended Roles", NamedTextColor.GRAY)),
                    DataComponentPair.lore(
                            Component.text("Townsfolk: ", NamedTextColor.GRAY)
                                    .append(Component.text((int) Math.ceil(numPlayers/3.0)*2-1, RoleInfo.ROLE_COLORS.get(Role.Type.TOWNSFOLK))),
                            Component.text("Outsiders: ", NamedTextColor.GRAY)
                                    .append(Component.text(numPlayers < 7 ? numPlayers%5 : (numPlayers-1)%6, RoleInfo.ROLE_COLORS.get(Role.Type.OUTSIDER))),
                            Component.text("Minions: ", NamedTextColor.GRAY)
                                    .append(Component.text(numPlayers < 10 ? 1 : numPlayers < 13 ? 2 : 3, RoleInfo.ROLE_COLORS.get(Role.Type.MINION))),
                            Component.text("Demons: ", NamedTextColor.GRAY)
                                    .append(Component.text(1, RoleInfo.ROLE_COLORS.get(Role.Type.DEMON)))));
        } else {
            for (int i = 0; i < (viewingScript == null ? 8 : 9); i++) {
                contents[i+45] = i < roles.get(Role.Type.TRAVELLER).size() ? roles.get(Role.Type.TRAVELLER).get(i).getItem() : TRAVELLER_FILLER;
            }

            if (viewingScript == null) contents[53] = RETURN;
            else contents[53] = selectedRoles.size() == rolesToSelect
                    ? CONTINUE_ENABLED
                    : DataComponentPair.lore(Component.text("Must select "+rolesToSelect+" players", NamedTextColor.DARK_GRAY))
                        .apply(CONTINUE_DISABLED.clone());
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

        if (inventory.getSize() == 45 && !selectingRoles) {
            if (NEW.equals(evt.getCurrentItem())) {
                selected = -1;
                newScript();
            } else if (CONTINUE_ENABLED.equals(evt.getCurrentItem())) {
                selectScript();
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
                renderViewScript(Component.text("Viewing Script - "+scripts.get(selected)));
            } else if (!EMPTY.equals(evt.getCurrentItem()) && (evt.getSlot() < 9 || (evt.getSlot() >= 18 && evt.getSlot() < 27))) {
                int idx = page * 18 + (evt.getSlot()/18)*9 + (evt.getSlot()%9);
                selected = selected == idx ? -1 : idx;
                renderPage();
            }
        } else {
            if (RETURN.equals(evt.getCurrentItem())) {
                player.openInventory(inventory = Bukkit.createInventory(null, 45, Component.text("Script Display")));
                renderPage();
            } else if (CONTINUE_ENABLED.equals(evt.getCurrentItem())) {
                if (viewingScript == null) {
                    selectRoles();
                } else {
                    new CustomPayloadEvent(selectedRoles).callEvent();

                    HandlerList.unregisterAll(this);
                    inventory.close();
                }
            } else if ((selectingRoles || viewingScript != null) && Material.PAPER == Optional.ofNullable(evt.getCurrentItem()).map(ItemStack::getType).orElse(null)) {
                String roleId = evt.getCurrentItem().getData(DataComponentTypes.CUSTOM_MODEL_DATA).strings().getFirst();
                if (roleId == null) return;

                RoleInfo role = RoleInfo.valueOf(roleId.toUpperCase());

                if (selectedRoles.contains(role)) {
                    selectedRoles.remove(role);
                } else {
                    selectedRoles.add(role);
                }

                renderViewScript(null);
            }
        }
    }

    private void selectRoles() {
        HandlerList.unregisterAll(this);
        inventory.close();

        rolesFuture.complete(selectedRoles);
    }

    private void selectScript() {
        selectingRoles = true;
        renderViewScript(Component.text("Select Roles in Play"));

        scriptFuture.complete((ScriptInfo) config.get(scripts.get(selected)));
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
        player.getInventory().setItem(EquipmentSlot.HAND, createItem(Material.WRITABLE_BOOK,
                DataComponentPair.name(Component.text("Script")),
                DataComponentPair.of(DataComponentTypes.WRITABLE_BOOK_CONTENT, WritableBookContent.writeableBookContent().addPages(pages).build()),
                DataComponentPair.custom(Pair.of(BOTC_BOOK, ByteTag.valueOf(true)))));
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

        player.getInventory().setItem(EquipmentSlot.HAND, createItem(Material.WRITABLE_BOOK,
                DataComponentPair.name(Component.text("Script")),
                DataComponentPair.custom(Pair.of(BOTC_BOOK, ByteTag.valueOf(true)))));
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
        if (!player.equals(evt.getPlayer()) || DataComponentPair.getCustomData(evt.getItemDrop().getItemStack(), BOTC_BOOK) == null) return;

        evt.getItemDrop().remove();
        finishScript(null);
    }

    @EventHandler
    public void onBook(PlayerEditBookEvent evt) {
        if (!player.equals(evt.getPlayer())) return;

        finishScript(String.join("", evt.getNewBookMeta().getPages()));
    }

    public static void open(Player player, int numPlayers, CompletableFuture<ScriptInfo> script, CompletableFuture<List<RoleInfo>> roles) throws ExecutionException, InterruptedException {
        Inventory inventory = Bukkit.createInventory(null, 45, Component.text("Script Display"));

        ScriptDisplay sd = new ScriptDisplay(player, inventory, numPlayers, script, roles);
        Bukkit.getPluginManager().registerEvents(sd, BloodOnTheClocktower.instance);
        sd.renderPage();

        Bukkit.getScheduler().callSyncMethod(BloodOnTheClocktower.instance, () -> player.openInventory(inventory)).get();
    }

    public static void viewRoles(Player player, ScriptInfo script, int numToSelect, Component title) {
        Inventory inventory = Bukkit.createInventory(null, 54, Component.text("Script Display"));

        ScriptDisplay sd = new ScriptDisplay(player, inventory, 0, null, null);
        Bukkit.getPluginManager().registerEvents(sd, BloodOnTheClocktower.instance);
        sd.viewingScript = script;
        sd.rolesToSelect = numToSelect;

        Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, () -> sd.renderViewScript(title));
    }
}
