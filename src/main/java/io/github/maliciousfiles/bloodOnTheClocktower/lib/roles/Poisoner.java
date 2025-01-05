package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;

import java.util.List;
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
            game.log("{0} is no longer poisoned", me, Game.LogPriority.MEDIUM, poisonedReminder.target);
        }
        moveReminderToken(poisonedReminder, null);
    }

    @Override
    public void handleNight() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> instruction = me.giveInstruction("Choose a player to poison");

        MinecraftHook<List<BOTCPlayer>> hook = new SelectPlayerHook(me, game, 1, _->true)
                .cancellable(game.getStoryteller().CANCEL, "cancel Poisoner action");

        List<BOTCPlayer> poisoned = hook.get();
        instruction.complete(null);
        if (hook.isCancelled()) return;
        BOTCPlayer poisonedPlayer = poisoned.getFirst();

        if (!me.isImpaired()) {
            moveReminderToken(poisonedReminder, poisonedPlayer);
            game.log("poisoned {0}", me, Game.LogPriority.HIGH, poisonedPlayer);
        } else {
            game.log("attempted to poison {0}", me, Game.LogPriority.MEDIUM, poisonedPlayer);
        }
    }
}
