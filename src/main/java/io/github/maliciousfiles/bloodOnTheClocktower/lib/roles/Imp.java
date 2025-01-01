package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.PlayerChoiceHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Imp extends Role {
    private ReminderToken deadReminder;

    public Imp(BOTCPlayer me, Game game, RoleInfo info) { super(me, game, info); }

    @Override
    protected boolean hasNightAction(Game game) {
        return game.getTurn() != 1;
    }

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
        me.giveInstruction("Choose a player to kill");

        BOTCPlayer dead = new SelectPlayerHook(me, game, 1, _->true)
                .get().getFirst();
        if (!me.isImpaired()) {
            moveReminderToken(deadReminder, dead);
            dead.handleDeathAttempt(BOTCPlayer.DeathCause.PLAYER, me);
        }
    }

    @Override
    public void handleDeathAttempt(BOTCPlayer.DeathCause cause, BOTCPlayer killer) throws ExecutionException, InterruptedException {
        if (cause == BOTCPlayer.DeathCause.PLAYER && killer == me) {
            new PlayerChoiceHook(game, "Pick a minion for the Imp to jump to")
                    .get().switchRole(info);
        }
        super.handleDeathAttempt(cause, killer);
    }
}
