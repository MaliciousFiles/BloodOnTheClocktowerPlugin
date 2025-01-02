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
        moveReminderToken(poisonedReminder, null);
    }

    @Override
    public void handleNight() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> instruction = me.giveInstruction("Choose a player to poison");

        BOTCPlayer poisoned = new SelectPlayerHook(me, game, 1, _->true)
                .get().getFirst();
        instruction.complete(null);
        if (!me.isImpaired()) {
            moveReminderToken(poisonedReminder, poisoned);
        }
    }
}
