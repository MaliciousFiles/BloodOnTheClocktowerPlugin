package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.ReminderToken;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.RoleInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.PlayerChoiceHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.RoleChoiceHook;
import io.github.maliciousfiles.bloodOnTheClocktower.util.CustomPayloadEvent;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Pair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class Grimoire implements Listener {
    public enum Access { STORYTELLER, PLAYER_SELECT, SPY }

    private final ItemStack FILLER = createItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text(" ")));
    private final ItemStack DARK_FILLER = createItem(Material.GRAY_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text(" ")));
    private final ItemStack EMPTY = createItem(Material.GRAY_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("Empty", NamedTextColor.GRAY)),
            DataComponentPair.lore(Component.text("No one sits here", NamedTextColor.DARK_GRAY)));
    private final ItemStack EMPTY_HEAD = createItem(Material.GRAY_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("Empty", NamedTextColor.GRAY)),
            DataComponentPair.lore(Component.text("No reminder tokens", NamedTextColor.DARK_GRAY)));
    private final ItemStack EMPTY_TARGET = createItem(Material.GRAY_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("No Target", NamedTextColor.GRAY)),
            DataComponentPair.lore(Component.text("Click to move reminder token (close selection inventory to select no-one)", NamedTextColor.DARK_GRAY)));

    private final ItemStack KILL_DISABLED = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("Kill", NamedTextColor.GRAY)),
            DataComponentPair.lore(Component.text("Instantly kill this player", NamedTextColor.DARK_GRAY)),
            DataComponentPair.of(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("stone_sword")));
    private final ItemStack KILL_ENABLED = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("Kill", TextColor.color(255, 71, 36))),
            DataComponentPair.lore(Component.text("Instantly kill this player", NamedTextColor.GRAY)),
            DataComponentPair.of(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("iron_sword")));
    private final ItemStack EXECUTE_DISABLED = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("Execute", NamedTextColor.GRAY)),
            DataComponentPair.lore(Component.text("Instantly execute this player", NamedTextColor.DARK_GRAY)),
            DataComponentPair.of(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("stone_axe")));
    private final ItemStack EXECUTE_ENABLED = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("Execute", TextColor.color(177, 20, 21))),
            DataComponentPair.lore(Component.text("Instantly execute this player", NamedTextColor.GRAY)),
            DataComponentPair.of(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("diamond_axe")));
    private final ItemStack REVIVE_DISABLED = createItem(Material.GRAY_STAINED_GLASS_PANE,
            DataComponentPair.name(Component.text("Revive", NamedTextColor.GRAY)),
            DataComponentPair.lore(Component.text("Bring the player back to life", NamedTextColor.DARK_GRAY)));
    private final ItemStack REVIVE_ENABLED = createItem(Material.TOTEM_OF_UNDYING,
            DataComponentPair.name(Component.text("Revive", TextColor.color(202, 148, 75))),
            DataComponentPair.lore(Component.text("Bring the player back to life", NamedTextColor.GRAY)));
    private final ItemStack CHANGE_ROLE = createItem(Material.WRITABLE_BOOK,
            DataComponentPair.name(Component.text("Change Role", TextColor.color(175, 83, 202))),
            DataComponentPair.lore(Component.text("Does not inform them; manually wake if necessary", NamedTextColor.GRAY)));
    private final ItemStack CHANGE_ALIGNMENT_GOOD = createItem(Material.BLUE_CONCRETE,
            DataComponentPair.name(Component.text("Make Evil", TextColor.color(202, 32, 40))),
            DataComponentPair.lore(Component.text("Does not inform them; manually wake if necessary", NamedTextColor.GRAY)));
    private final ItemStack CHANGE_ALIGNMENT_EVIL = createItem(Material.RED_CONCRETE,
            DataComponentPair.name(Component.text("Make Good", TextColor.color(62, 111, 202))),
            DataComponentPair.lore(Component.text("Does not inform them; manually wake if necessary", NamedTextColor.GRAY)));
    private final ItemStack WAKE = createItem(Material.LIGHT,
            DataComponentPair.name(Component.text("Wake", TextColor.color(209, 189, 44))),
            DataComponentPair.lore(Component.text("Awaken this player", NamedTextColor.GRAY)));
    private final ItemStack SLEEP = createItem(Material.RED_BED,
            DataComponentPair.name(Component.text("Sleep", TextColor.color(1, 0, 201))),
            DataComponentPair.lore(Component.text("Put this player to sleep", NamedTextColor.GRAY)));
    private final ItemStack DEAD_VOTE_DISABLED = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("Grant Dead Vote", NamedTextColor.GRAY)),
            DataComponentPair.lore(Component.text("Give the player back their dead vote", NamedTextColor.DARK_GRAY)),
            DataComponentPair.model("nominate"),
            DataComponentPair.cmd(false));
    private final ItemStack DEAD_VOTE_ENABLED = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("Grant Dead Vote", TextColor.color(104, 215, 250))),
            DataComponentPair.lore(Component.text("Give the player back their dead vote", NamedTextColor.GRAY)),
            DataComponentPair.model("nominate"),
            DataComponentPair.cmd(true));
    private final ItemStack DRUNK = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("Drunk", TextColor.color(74, 97, 200))),
            DataComponentPair.lore(Component.text("Toggle the Storyteller Drunk token", NamedTextColor.GRAY)),
            DataComponentPair.model("role"),
            DataComponentPair.cmd("drunk"));
    private final ItemStack SOBER = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("Sober", TextColor.color(73, 200, 114))),
            DataComponentPair.lore(Component.text("Toggle the Storyteller Sober and Healthy token", NamedTextColor.GRAY)),
            DataComponentPair.model("role"),
            DataComponentPair.cmd("barista"));

    private final ItemStack RETURN = createItem(Material.BARRIER,
            DataComponentPair.name(Component.text("Return", NamedTextColor.RED)));

    private static final NamespacedKey IDX = BloodOnTheClocktower.key("grimoire_idx");

    private CompletableFuture<Integer> select;

    private Access access;
    private final PlayerWrapper player;
    private final Game game;
    private Inventory inventory;
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
    private int editing = -1;

    private ItemStack createRole(int i) {
        BOTCPlayer owner = seatOrder.get(i);
        if (owner == null) return EMPTY;

        ItemStack item = owner.getRoleInfo().getItem(owner.reminderTokensOnMe.isEmpty() ? Material.PAPER : Material.BUNDLE);
        item.setData(DataComponentTypes.BUNDLE_CONTENTS, BundleContents.bundleContents(owner.reminderTokensOnMe.stream().map(ReminderToken::getItem).toList()));
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, selected == i);
        item = DataComponentPair.custom(Pair.of(IDX, IntTag.valueOf(i))).apply(item);

        List<Component> lore = new ArrayList<>(item.getData(DataComponentTypes.LORE).lines());
        lore.add(Component.empty());
        lore.add(Component.text("Click to view associated reminder tokens", NamedTextColor.GRAY));
        DataComponentPair.lore(lore.toArray(Component[]::new)).apply(item);

        return item;
    }

    private ItemStack createHead(int i) {
        BOTCPlayer owner = seatOrder.get(i);
        if (owner == null) return FILLER;

        if (selected != -1) {
            BOTCPlayer selectedPlayer = seatOrder.get(selected);
            List<ItemStack> tokens = owner.reminderTokensOnMe.stream().filter(t -> t.source == selectedPlayer).map(ReminderToken::getItem).toList();
            if (tokens.isEmpty()) {
                return EMPTY_HEAD;
            } else {
                if (tokens.size() == 1) {
                    return tokens.getFirst();
                } else {
                    return createItem(Material.BUNDLE,
                            DataComponentPair.name(Component.text((owner.getName()+"'s Reminder Tokens From "+selectedPlayer.getName()).replace("s's", "s'"))),
                            DataComponentPair.of(DataComponentTypes.BUNDLE_CONTENTS, BundleContents.bundleContents(tokens)));
                }
            }
        }

        ItemStack item = createItem(Material.PLAYER_HEAD,
                DataComponentPair.of(DataComponentTypes.PROFILE,
                        ResolvableProfile.resolvableProfile(owner.getPlayer().getPlayerProfile())),
                DataComponentPair.of(DataComponentTypes.CUSTOM_NAME, Component.text(owner.getName())
                        .decoration(TextDecoration.ITALIC, false)),
                DataComponentPair.custom(Pair.of(IDX, IntTag.valueOf(i))));

        return DataComponentPair.lore(Component.text(access == Access.PLAYER_SELECT
                    ? "Click to select player"
                    : editing == -1 ? "Click to edit player"
                    : "Click to move reminder token (close selection inventory to select no-one)", NamedTextColor.GRAY)).apply(item);
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

    private void renderEditPlayer() {
        BOTCPlayer selectedPlayer = seatOrder.get(editing);

        ItemStack[] contents = inventory.getContents();
        Arrays.fill(contents, FILLER);

        contents[0] = createHead(editing);
        contents[1] = createRole(editing);
        for (int i = 2; i < 8; i++) contents[i] = DARK_FILLER;
        contents[8] = RETURN;

        for (int i = 0; i < 9; i++) contents[i+9] = DARK_FILLER;

        contents[18] = selectedPlayer.isAlive() ? KILL_ENABLED : KILL_DISABLED;
        contents[19] = selectedPlayer.isAlive() ? EXECUTE_ENABLED : EXECUTE_DISABLED;
        contents[20] = !selectedPlayer.isAlive() ? REVIVE_ENABLED : REVIVE_DISABLED;
        contents[21] = CHANGE_ROLE;
        contents[22] = selectedPlayer.getAlignment() == BOTCPlayer.Alignment.GOOD ? CHANGE_ALIGNMENT_GOOD : CHANGE_ALIGNMENT_EVIL;
        contents[23] = selectedPlayer.isAwake() ? SLEEP : WAKE;
        contents[24] = !selectedPlayer.hasDeadVote() ? DEAD_VOTE_ENABLED : DEAD_VOTE_DISABLED;
        contents[25] = DataComponentPair.of(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
                selectedPlayer.reminderTokensOnMe.contains(ReminderToken.STORYTELLER_DRUNK)).apply(DRUNK);
        contents[26] = DataComponentPair.of(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
                selectedPlayer.reminderTokensOnMe.contains(ReminderToken.STORYTELLER_SOBER_AND_HEALTHY)).apply(SOBER);

        for (int i = 0; i < 9; i++) contents[i+27] = DARK_FILLER;

        for (int i = 0; i < 9; i++) {
            if (i >= selectedPlayer.getMyReminderTokens().size()) continue;
            ReminderToken token = selectedPlayer.getMyReminderTokens().get(i);

            contents[i+36] = token.getItem();
            contents[i+45] = token.target == null ? EMPTY_TARGET : createHead(seatOrder.indexOf(token.target));
        }

        inventory.setContents(contents);
    }

    @EventHandler
    public void inventoryInteract(InventoryClickEvent evt) throws ExecutionException, InterruptedException {
        if (!inventory.equals(evt.getClickedInventory())) return;
        evt.setCancelled(true);

        if (editing == -1 || access == Access.PLAYER_SELECT) {
            int idx = Optional.ofNullable(evt.getCurrentItem()).map(i->DataComponentPair.<IntTag>getCustomData(i, IDX)).map(IntTag::getAsInt).orElse(-1);
            if (idx == -1) return;

            if (evt.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                if (access == Access.PLAYER_SELECT) {
                    if (select != null) {
                        select.complete(idx);
                        return;
                    }

                    new CustomPayloadEvent(seatOrder.get(idx)).callEvent();
                    HandlerList.unregisterAll(this);
                    inventory.close();
                } else if (access == Access.STORYTELLER) {
                    selected = -1;
                    editing = idx;

                    renderEditPlayer();
                }

                return;
            }

            if (selected == idx) selected = -1;
            else selected = idx;
            render();

            return;
        }

        if (RETURN.equals(evt.getCurrentItem())) {
            editing = -1;
            render();
        } else if (KILL_ENABLED.equals(evt.getCurrentItem())) {
            seatOrder.get(editing).die();
            inventory.close();
        } else if (EXECUTE_ENABLED.equals(evt.getCurrentItem())) {
            Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
                try {
                    seatOrder.get(editing).execute(true);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            inventory.close();
        } else if (REVIVE_ENABLED.equals(evt.getCurrentItem())) {
            seatOrder.get(editing).revive();
            inventory.close();
        } else if (CHANGE_ROLE.equals(evt.getCurrentItem())) {
            Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
                try {
                    ignoreClose = true;
                    RoleInfo role = new RoleChoiceHook(player, game, "Select new role for "+seatOrder.get(editing).getName(), 1).get().getFirst();
                    ignoreClose = false;

                    Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, () -> player.getPlayer().openInventory(inventory));
                    seatOrder.get(editing).changeRoleAndAlignment(role, role.alignment());
                    seatOrder.get(editing).updateRoleItem();

                    renderEditPlayer();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (CHANGE_ALIGNMENT_GOOD.equals(evt.getCurrentItem())) {
            seatOrder.get(editing).changeRoleAndAlignment(null, BOTCPlayer.Alignment.EVIL);

            renderEditPlayer();
        } else if (CHANGE_ALIGNMENT_EVIL.equals(evt.getCurrentItem())) {
            seatOrder.get(editing).changeRoleAndAlignment(null, BOTCPlayer.Alignment.GOOD);

            renderEditPlayer();
        } else if (WAKE.equals(evt.getCurrentItem())) {
            seatOrder.get(editing).wake();
            renderEditPlayer();
        } else if (SLEEP.equals(evt.getCurrentItem())) {
            seatOrder.get(editing).sleep();
            game.getSeats().forceSit(seatOrder.get(editing));
            renderEditPlayer();
        } else if (DEAD_VOTE_ENABLED.equals(evt.getCurrentItem())) {
            seatOrder.get(editing).returnDeadVote();
            renderEditPlayer();
        } else if (DRUNK.equals(evt.getCurrentItem())) {
            List<ReminderToken> tokens = seatOrder.get(editing).reminderTokensOnMe;
            if (tokens.contains(ReminderToken.STORYTELLER_DRUNK)) tokens.remove(ReminderToken.STORYTELLER_DRUNK);
            else tokens.add(ReminderToken.STORYTELLER_DRUNK);

            renderEditPlayer();
        } else if (SOBER.equals(evt.getCurrentItem())) {
            List<ReminderToken> tokens = seatOrder.get(editing).reminderTokensOnMe;
            if (tokens.contains(ReminderToken.STORYTELLER_SOBER_AND_HEALTHY)) tokens.remove(ReminderToken.STORYTELLER_SOBER_AND_HEALTHY);
            else tokens.add(ReminderToken.STORYTELLER_SOBER_AND_HEALTHY);

            renderEditPlayer();
        } else if (evt.getSlot() >= 45 && evt.getSlot()-45 < seatOrder.get(editing).getMyReminderTokens().size()) {
            Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
                try {
                    select = new CompletableFuture<>();

                    access = Access.PLAYER_SELECT;
                    render();

                    Integer val = select.get();

                    access = Access.STORYTELLER;
                    select = null;

                    seatOrder.get(editing).moveReminderToken(
                            seatOrder.get(editing).getMyReminderTokens().get(evt.getSlot()-45),
                            val == null ? null : seatOrder.get(val));
                    renderEditPlayer();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private boolean ignoreClose = false;
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent evt) {
        if (ignoreClose) return;

        if (!inventory.equals(evt.getInventory())) return;
        if (select != null) {
            Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, () -> evt.getPlayer().openInventory(inventory));
            select.complete(null);
            return;
        }

        Bukkit.getScheduler().runTaskLater(BloodOnTheClocktower.instance, () -> {
            if (!inventory.equals(evt.getPlayer().getOpenInventory().getTopInventory())) HandlerList.unregisterAll(this);
        }, 2);
    }

    private static final NamespacedKey GAME_ID = BloodOnTheClocktower.key("game_id");
    private static final ItemStack GRIMOIRE = createItem(Material.PAPER,
            DataComponentPair.model("grimoire"),
            DataComponentPair.name(Component.text("Grimoire", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)),
            DataComponentPair.lore(Component.text("Right-click to open the Storyteller's Grimoire", NamedTextColor.GRAY)));

    public static ItemStack createGrimoire(Game game) {
        return DataComponentPair.custom(Pair.of(GAME_ID, StringTag.valueOf(game.getId().toString()))).apply(GRIMOIRE.clone());
    }
    public static Inventory openInventory(Game game, Player player, Access access, Component title) {
        PlayerWrapper botcPlayer = player == game.getStoryteller().getPlayer() ? game.getStoryteller()
                : game.getBOTCPlayer(player);
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
                if (!evt.getAction().isRightClick() || evt.getItem() == null) return;
                String gameId = Optional.ofNullable(DataComponentPair.<StringTag>getCustomData(evt.getItem(), GAME_ID)).map(StringTag::getAsString).orElse(null);
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
