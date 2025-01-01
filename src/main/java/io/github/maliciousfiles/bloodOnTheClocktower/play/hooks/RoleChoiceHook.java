package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

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
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;

import java.util.concurrent.CompletableFuture;

public class RoleChoiceHook extends MinecraftHook<RoleInfo> {
    public RoleChoiceHook(Game game, Storyteller storyteller, CompletableFuture<RoleInfo> complete) {
        super(complete);

        ScriptDisplay.viewRoles(storyteller.getPlayer(), game.getScript(), Component.text("Choose a Role", PlayerWrapper.INSTRUCTION_COLOR, TextDecoration.BOLD));
    }

    @EventHandler
    protected void onEvent(CustomPayloadEvent evt) {
        if (evt.source() == ScriptDisplay.class && evt.data() instanceof RoleInfo role) complete(role);
    }
}