package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Monk extends Role {
    private ReminderToken safeReminder;

    public Monk(BOTCPlayer me, Game game, RoleInfo info) { super(me, game, info); }

    @Override
    protected boolean hasNightAction() {
        return game.getTurn() != 1;
    }

    @Override
    public void setup() {
        newReminderToken(safeReminder = new ReminderToken("Safe", me, null, ReminderToken.Effect.SAFE_FROM_DEMON));
    }

    @Override
    public void handleDawn() {
        if (safeReminder.target != null) {
            game.log("{0} is no longer protected", me, Game.LogPriority.MEDIUM, safeReminder.target);
        }
        moveReminderToken(safeReminder, null);
    }

    @Override
    public void handleNight() throws InterruptedException, ExecutionException {
        CompletableFuture<Void> instruction = me.giveInstruction("Choose a player to protect");

        MinecraftHook<List<BOTCPlayer>> hook = new SelectPlayerHook(me, game, 1, p -> p != me)
                .cancellable(game.getStoryteller().CANCEL, "cancel Monk action");

        List<BOTCPlayer> safe = hook.get();
        instruction.complete(null);
        if (hook.isCancelled()) return;
        BOTCPlayer safePlayer = safe.getFirst();

        if (!me.isImpaired()) {
            moveReminderToken(safeReminder, safePlayer);
            game.log("protected {0}", me, Game.LogPriority.HIGH, safePlayer);
        } else {
            game.log("attempted to protect {0}", me, Game.LogPriority.MEDIUM, safePlayer);
        }
    }
}
