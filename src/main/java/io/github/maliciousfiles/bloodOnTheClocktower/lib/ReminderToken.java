package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class ReminderToken {
    public enum Effect { NONE, DRUNK, POISONED, HAS_ABILITY, SOBER_AND_HEALTHY }

    public final String name;
    public final BOTCPlayer source;
    public final Effect effect;
    @Nullable public BOTCPlayer target;

    public ReminderToken(String name, BOTCPlayer source, @Nullable BOTCPlayer target, Effect effect) {
        this.name = name;
        this.source = source;
        this.target = target;
        this.effect = effect;
    }

    public ItemStack getItem() {
        return source.getRole().info.getItem(name, false);
    }

    public boolean isFunctioning() {
        return !source.isImpaired();
    }

    public Effect getEffect() {
        return isFunctioning() ? effect : Effect.NONE;
    }
}
