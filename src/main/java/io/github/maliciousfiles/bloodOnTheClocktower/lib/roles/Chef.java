package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ChatComponents;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.PlayerChoiceHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.RoleChoiceHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.StorytellerPauseHook;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Chef extends Role {
    boolean hasInfo = false;

    public Chef(BOTCPlayer me, Game game, RoleInfo info) { super(me, game, info); }

    @Override
    protected boolean hasNightAction() {
        return !hasInfo;
    }

    @Override
    public void handleNight() throws ExecutionException, InterruptedException {
        if (hasInfo) { return; }
        hasInfo = true;

        CompletableFuture<Void> instruction = game.getStoryteller().giveInstruction("Tell the Chef " + (me.isImpaired() ? " (impaired)" : "") + " the number of pairs of evil players");
        // TODO: add a storyteller number choice hook

        game.log("learned that there are ___ pairs of evil players", me, Game.LogPriority.HIGH);
        me.giveInfo(Component.text("There are ___ pairs of evil players"));
        instruction.complete(null);
    }
}
