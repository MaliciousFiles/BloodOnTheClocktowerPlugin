package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Role;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.StatusEffect;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Poisoner implements Role {
    public static final Poisoner INSTANCE = new Poisoner();

    @Override
    public String getRoleName() {
        return "Poisoner";
    }

    @Override
    public String getRoleDescription() {
        return "Each night, choose a player: they are poisoned tonight and tomorrow day.";
    }

    @Override
    public Material getIcon() {
        return Material.SPLASH_POTION;
    }

    @Override
    public float getFirstNightOrder() {
        return 23;
    }

    @Override
    public float getNormalNightOrder() {
        return 11;
    }

    @Override
    public void doNightAction(BOTCPlayer me, Game game) throws InterruptedException, ExecutionException {
        me.giveInstruction("Choose a player to poison");

        CompletableFuture<List<BOTCPlayer>> future = new CompletableFuture<>();
        new SelectPlayerHook(me, game, 1, _->true, future);

        BOTCPlayer poisoned = future.get().getFirst();

        if (!me.isMalfunctioning()) {
            poisoned.addStatusEffect(new StatusEffect(StatusEffect.EffectType.POISONED,
                    1, StatusEffect.EndsOn.DAY, me));
        }
    }
}
