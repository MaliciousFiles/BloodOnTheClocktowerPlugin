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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PlayerChoiceHook extends MinecraftHook<BOTCPlayer> {
    private final Inventory grimoire;

    public PlayerChoiceHook(Game game, String instruction) throws ExecutionException, InterruptedException {
        grimoire = Bukkit.getScheduler().callSyncMethod(BloodOnTheClocktower.instance, () -> Grimoire.openInventory(game, game.getStoryteller().getPlayer(), Grimoire.Access.PLAYER_SELECT,
                Component.text(instruction, NamedTextColor.DARK_RED))).get();
    }

    @EventHandler
    public void onClick(CustomPayloadEvent evt) {
        if (evt.source() == Grimoire.class && evt.data() instanceof BOTCPlayer player) complete(player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent evt) {
        if (evt.getReason() == InventoryCloseEvent.Reason.PLAYER && grimoire.equals(evt.getInventory())) Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, () -> evt.getPlayer().openInventory(grimoire));
    }

    @Override
    protected void cleanup() {
        Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, grimoire::close);
    }
}
