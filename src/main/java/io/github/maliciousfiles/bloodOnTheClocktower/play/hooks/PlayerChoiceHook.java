package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import io.github.maliciousfiles.bloodOnTheClocktower.play.Grimoire;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.github.maliciousfiles.bloodOnTheClocktower.util.CustomPayloadEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.concurrent.CompletableFuture;

public class PlayerChoiceHook extends MinecraftHook<BOTCPlayer> {
    private final Inventory grimoire;

    public PlayerChoiceHook(Game game, Storyteller storyteller, CompletableFuture<BOTCPlayer> complete) {
        super(complete);

        grimoire = Grimoire.openInventory(game, storyteller.getPlayer(), Grimoire.Access.PLAYER_SELECT,
                Component.text("Choose a Player", PlayerWrapper.INSTRUCTION_COLOR, TextDecoration.BOLD));
    }

    @EventHandler
    public void onClick(CustomPayloadEvent evt) {
        if (evt.source() == Grimoire.class && evt.data() instanceof BOTCPlayer player) complete(player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent evt) {
        if (grimoire.equals(evt.getInventory())) Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, () -> evt.getPlayer().openInventory(grimoire));
    }
}
