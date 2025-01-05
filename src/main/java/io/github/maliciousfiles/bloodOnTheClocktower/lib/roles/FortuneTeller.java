package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ChatComponents;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.*;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FortuneTeller extends Role {
    private ReminderToken redHerringReminder;

    public FortuneTeller(BOTCPlayer me, Game game, RoleInfo info) { super(me, game, info); }

    @Override
    protected boolean hasNightAction() {
        return true;
    }

    @Override
    public void setup() throws ExecutionException, InterruptedException {
        new StorytellerPauseHook(game.getStoryteller(), "assign the Fortune Teller's Red Herring reminder token to a good player").get();

        BOTCPlayer redHerring = new PlayerChoiceHook(game, "Red Herring reminder token").get();
        newReminderToken(new ReminderToken("Red Herring", me, redHerring, ReminderToken.Effect.NONE));
    }

    @Override
    public void handleNight() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> instruction = me.giveInstruction("Choose two players to investigate");

        MinecraftHook<List<BOTCPlayer>> hook = new SelectPlayerHook(me, game, 2, _->true)
                .cancellable(game.getStoryteller().CANCEL, "cancel Fortune Teller action");

        List<BOTCPlayer> selected = hook.get();
        instruction.complete(null);
        if (hook.isCancelled()) return;

        boolean result = new StorytellerQuestionHook(game.getStoryteller(), "Pick an answer to give the Fortune Teller" + (me.isImpaired() ? " (impaired)" : "")).get();

        game.log("investigated {0} and {1} and got a " + (result ? "yes" : "no"), me, Game.LogPriority.HIGH, selected.get(0), selected.get(1));

        String players = selected.get(0).getName() + " and " + selected.get(1).getName();
        me.giveInfo(Component.text(result ? "There is a demon among " + players: "Neither of " + players + " is a demon"));
    }
}
