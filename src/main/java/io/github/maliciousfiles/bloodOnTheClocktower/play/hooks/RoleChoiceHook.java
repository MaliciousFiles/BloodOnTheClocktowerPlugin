package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.RoleInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import io.github.maliciousfiles.bloodOnTheClocktower.play.Grimoire;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ScriptDisplay;
import io.github.maliciousfiles.bloodOnTheClocktower.util.CustomPayloadEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RoleChoiceHook extends MinecraftHook<List<RoleInfo>> {
    private final Inventory inv;

    public RoleChoiceHook(PlayerWrapper player, Game game, String instruction, int number) {
        inv = ScriptDisplay.viewRoles(player.getPlayer(), game.getScript(), number, Component.text(instruction, NamedTextColor.DARK_RED));
    }

    @EventHandler
    protected void onEvent(CustomPayloadEvent evt) {
        if (evt.source() == ScriptDisplay.class && evt.data() instanceof List<?> roles) complete((List<RoleInfo>) roles);
    }

    @Override
    protected void cleanup() {
        Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, inv::close);
    }
}
