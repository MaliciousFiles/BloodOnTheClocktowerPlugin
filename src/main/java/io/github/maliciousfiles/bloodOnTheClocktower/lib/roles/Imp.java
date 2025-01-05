package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.PlayerChoiceHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;

import java.util.List;
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
        CompletableFuture<Void> instruction = me.giveInstruction("Choose a player to kill");

        MinecraftHook<List<BOTCPlayer>> hook = new SelectPlayerHook(me, game, 1, _->true)
                .cancellable(game.getStoryteller().CANCEL, "cancel choosing killed");

        List<BOTCPlayer> dead = hook.get();
        instruction.complete(null);
        if (hook.isCancelled()) return;
        BOTCPlayer deadPlayer = dead.getFirst();

        if (!me.isImpaired() && !deadPlayer.isSafeFromDemon()) {
            game.log("killed {0}", me, Game.LogPriority.HIGH, deadPlayer);
            moveReminderToken(deadReminder, deadPlayer);
            deadPlayer.handleDeathAttempt(BOTCPlayer.DeathCause.PLAYER, me);
        } else {
            game.log("attempted to kill {0}", me, Game.LogPriority.MEDIUM, deadPlayer);
        }
    }

    @Override
    public void handleDeathAttempt(BOTCPlayer.DeathCause cause, BOTCPlayer killer) throws ExecutionException, InterruptedException {
        if (cause == BOTCPlayer.DeathCause.PLAYER && killer == me) {
            BOTCPlayer newImp = new PlayerChoiceHook(game, "Pick a minion for the Imp to jump to").get();
            game.log("jumped to {1}", me, Game.LogPriority.HIGH, newImp);
            newImp.changeRoleAndAlignment(info, null);
        }
        super.handleDeathAttempt(cause, killer);
    }
}
