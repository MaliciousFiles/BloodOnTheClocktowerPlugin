package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.PlayerChoiceHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Imp extends Role {
    private ReminderToken deadReminder;

    public Imp(BOTCPlayer me, Game game, RoleInfo info) { super(me, game, info); }

    @Override
    protected boolean hasNightAction() {
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
        me.wake();
        CompletableFuture<Void> instruction = me.giveInstruction("Choose a player to kill");

        BOTCPlayer dead = new SelectPlayerHook(me, game, 1, _->true)
                .get().getFirst();
        instruction.complete(null);
        if (!me.isImpaired()) {
            game.log("{0} killed {1}", Game.LogPriority.HIGH, me, dead);
            moveReminderToken(deadReminder, dead);
            dead.handleDeathAttempt(BOTCPlayer.DeathCause.PLAYER, me);
        } else {
            game.log("{0} attempted to kill {1}", Game.LogPriority.MEDIUM, me, dead);
        }
    }

    @Override
    public void handleDeathAttempt(BOTCPlayer.DeathCause cause, BOTCPlayer killer) throws ExecutionException, InterruptedException {
        if (cause == BOTCPlayer.DeathCause.PLAYER && killer == me) {
            BOTCPlayer newImp = new PlayerChoiceHook(game, "Pick a minion for the Imp to jump to").get();
            game.log("{0} is jumping to {1}", Game.LogPriority.HIGH, me, newImp);
            newImp.changeRoleAndAlignment(info, null);
        }
        super.handleDeathAttempt(cause, killer);
    }
}
