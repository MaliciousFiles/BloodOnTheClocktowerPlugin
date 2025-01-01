package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class StorytellerPauseHook extends MinecraftHook<Void> {
    private static final ItemStack CONTINUE_ENABLED = createItem(Material.PAPER, meta -> {
        meta.displayName(Component.text("Continue", NamedTextColor.GREEN, TextDecoration.BOLD));
        meta.lore(List.of(Component.text("Right click to continue the game", NamedTextColor.GRAY)));

        meta.setCustomModelData(253);
    });
    private static final ItemStack CONTINUE_DISABLED = createItem(Material.PAPER, meta -> {
        meta.displayName(Component.text("Continue", NamedTextColor.GRAY, TextDecoration.BOLD));
        meta.lore(List.of(Component.text("Right click to continue the game", NamedTextColor.DARK_GRAY)));

        meta.setCustomModelData(254);
    });

    private final CompletableFuture<Void> instruction;

    public StorytellerPauseHook(Storyteller storyteller, String instruction) {
        this.instruction = storyteller.giveInstruction(instruction);
        storyteller.getPlayer().getInventory().setItem(8, CONTINUE_ENABLED);
    }

    @EventHandler
    public void rightClick(PlayerInteractEvent evt) {
        if (!CONTINUE_ENABLED.equals(evt.getItem())) return;

        evt.setCancelled(true);
        evt.getPlayer().getInventory().setItemInMainHand(CONTINUE_DISABLED);

        instruction.complete(null);
        complete(null);
    }
}
