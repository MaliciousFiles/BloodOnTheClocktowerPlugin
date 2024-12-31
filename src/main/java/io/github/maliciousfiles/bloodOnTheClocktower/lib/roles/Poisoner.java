package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Poisoner extends Role {
    private ReminderToken poisonedReminder;

    public Poisoner(BOTCPlayer me, Game game, RoleInfo info) { super(me, game, info); }

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
        me.giveInstruction(Component.text("Choose a player to poison"));

        CompletableFuture<List<BOTCPlayer>> future = new CompletableFuture<>();
        new SelectPlayerHook(me, game, 1, _->true, future);

        BOTCPlayer poisoned = future.get().getFirst();
        if (!me.isImpaired()) {
            moveReminderToken(poisonedReminder, poisoned);
        }
    }
}
