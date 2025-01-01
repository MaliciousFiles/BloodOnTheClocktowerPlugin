package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Imp extends Role {
    private ReminderToken deadReminder;

    public Imp(BOTCPlayer me, Game game, RoleInfo info) { super(me, game, info); }

    @Override
    public void setup() {
        newReminderToken(deadReminder = new ReminderToken("Dead", me, null, ReminderToken.Effect.NONE));
    }

    @Override
    public void handleDusk() {
        moveReminderToken(deadReminder, null);
    }

    @Override
    public void handleNight() throws InterruptedException, ExecutionException {
        if (game.getTurn() == 1) { return; }

        me.giveInstruction("Choose a player to kill");

        CompletableFuture<List<BOTCPlayer>> future = new CompletableFuture<>();
        new SelectPlayerHook(me, game, 1, _->true, future);

        BOTCPlayer dead = future.get().getFirst();
        if (!me.isImpaired()) {
            moveReminderToken(deadReminder, dead);
            dead.handleDeathAttempt(BOTCPlayer.DeathCause.PLAYER, me);
        }
    }

    @Override
    public void handleDeathAttempt(BOTCPlayer.DeathCause cause, BOTCPlayer killer) throws ExecutionException, InterruptedException {
        if (cause == BOTCPlayer.DeathCause.PLAYER && killer == me) {
            // TODO: jump to a minion
            CompletableFuture<BOTCPlayer> minion = new CompletableFuture<>();
//            new PlayerChoiceHook(game.getStoryteller(), game, "Pick a minion for the Imp to jump to", 1, null, minion);
            minion.get().switchRole(info);
        }
        super.handleDeathAttempt(cause, killer);
    }
}
