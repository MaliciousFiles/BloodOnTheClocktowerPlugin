package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Poisoner extends Role {
    private ReminderToken poisonedReminder;

    public Poisoner(BOTCPlayer me, Game game, RoleInfo info) { super(me, game, info); }

    @Override
    protected boolean hasNightAction() {
        return true;
    }

    @Override
    public void setup() {
        newReminderToken(poisonedReminder = new ReminderToken("Poisoned", me, null, ReminderToken.Effect.POISONED));
    }

    @Override
    public void handleDusk() {
        if (poisonedReminder.target != null) {
            game.log("{0} is no longer poisoned", Game.LogPriority.MEDIUM, poisonedReminder.target);
        }
        moveReminderToken(poisonedReminder, null);
    }

    @Override
    public void handleNight() throws InterruptedException, ExecutionException {
        me.wake();
        CompletableFuture<Void> instruction = me.giveInstruction("Choose a player to poison");

        BOTCPlayer poisoned = new SelectPlayerHook(me, game, 1, _->true)
                .get().getFirst();
        instruction.complete(null);
        if (!me.isImpaired()) {
            moveReminderToken(poisonedReminder, poisoned);
            game.log("{0} poisoned {1}", Game.LogPriority.HIGH, me, poisoned);
        } else {
            game.log("{0} attempted to poison {1}", Game.LogPriority.MEDIUM, me, poisoned);
        }
    }
}
