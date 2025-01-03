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

public class Washerwoman extends Role {
    boolean hasInfo = false;

    public Washerwoman(BOTCPlayer me, Game game, RoleInfo info) { super(me, game, info); }

    @Override
    protected boolean hasNightAction() {
        return !hasInfo;
    }

    @Override
    public void setup() throws ExecutionException, InterruptedException {
        new StorytellerPauseHook(game.getStoryteller(), "Continue to assign the Washerwoman's Townsfolk and Wrong reminder tokens").get();

        BOTCPlayer townsfolk = new PlayerChoiceHook(game, "Townsfolk reminder token").get();
        newReminderToken(new ReminderToken("Townsfolk", me, townsfolk, ReminderToken.Effect.NONE));

        BOTCPlayer wrong = new PlayerChoiceHook(game, "Wrong reminder token").get();
        newReminderToken(new ReminderToken("Wrong", me, wrong, ReminderToken.Effect.NONE));
    }

    @Override
    public void handleNight() throws ExecutionException, InterruptedException {
        if (hasInfo) { return; }
        hasInfo = true;

        me.wake();

        CompletableFuture<Void> instruction = game.getStoryteller().giveInstruction("Select two players for the Washerwoman");

        List<BOTCPlayer> players = new SelectPlayerHook(game.getStoryteller(), game, 2, p->!p.equals(me)).get();
        RoleInfo role = new RoleChoiceHook(game.getStoryteller(), game, "Pick a role to show the Washerwoman", 1).get().getFirst();

        me.giveInfo(Component.text("One of " + players.get(0).getName() + " and " + players.get(1).getName() + " is a ").append(ChatComponents.roleInfo(role)));
        instruction.complete(null);
    }
}
