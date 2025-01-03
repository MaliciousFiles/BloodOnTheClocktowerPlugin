package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class StorytellerQuestionHook extends MinecraftHook<Boolean> {
    private static final ItemStack YES_ENABLED = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("Yes", TextColor.color(24, 255, 45), TextDecoration.BOLD)),
            DataComponentPair.lore(Component.text("Right click to answer yes", NamedTextColor.GRAY)),
            DataComponentPair.model("yes"),
            DataComponentPair.cmd(true));
    private static final ItemStack YES_DISABLED = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("Yes", NamedTextColor.GRAY, TextDecoration.BOLD)),
            DataComponentPair.lore(Component.text("Right click to answer yes", NamedTextColor.DARK_GRAY)),
            DataComponentPair.model("yes"),
            DataComponentPair.cmd(false));

    private static final ItemStack NO_ENABLED = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("No", TextColor.color(255, 21, 21), TextDecoration.BOLD)),
            DataComponentPair.lore(Component.text("Right click to answer no", NamedTextColor.GRAY)),
            DataComponentPair.model("no"),
            DataComponentPair.cmd(true));
    private static final ItemStack NO_DISABLED = createItem(Material.PAPER,
            DataComponentPair.name(Component.text("No", NamedTextColor.GRAY, TextDecoration.BOLD)),
            DataComponentPair.lore(Component.text("Right click to answer no", NamedTextColor.DARK_GRAY)),
            DataComponentPair.model("no"),
            DataComponentPair.cmd(false));

    private final Player player;
    private final CompletableFuture<Void> instruction;

    public StorytellerQuestionHook(Storyteller storyteller, String instruction) {
        this.instruction = storyteller.askQuestion(instruction);
        (player = storyteller.getPlayer()).getInventory().setItem(7, YES_ENABLED);
        player.getInventory().setItem(6, NO_ENABLED);
    }

    @EventHandler
    public void rightClick(PlayerInteractEvent evt) {
        if (!player.equals(evt.getPlayer()) || (!YES_ENABLED.equals(evt.getItem()) && !NO_ENABLED.equals(evt.getItem())) || !evt.getAction().isRightClick()) return;

        evt.setCancelled(true);
        player.getInventory().setItem(7, YES_DISABLED);
        player.getInventory().setItem(6, NO_DISABLED);

        instruction.complete(null);
        complete(YES_ENABLED.equals(evt.getItem()));
    }
}
