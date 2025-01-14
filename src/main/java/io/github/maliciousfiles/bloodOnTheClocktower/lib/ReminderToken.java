package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class ReminderToken {
    public static final ReminderToken STORYTELLER_DRUNK = new ReminderToken("[ST] Drunk", null, null, Effect.DRUNK) {
        public ItemStack getItem() { return RoleInfo.DRUNK.getItem(Material.PAPER, name, false, false); }
        public boolean isFunctioning() { return true; }
    };
    public static final ReminderToken STORYTELLER_SOBER_AND_HEALTHY = new ReminderToken("[ST] Sober and Healthy", null, null, Effect.SOBER_AND_HEALTHY) {
        public ItemStack getItem() { return RoleInfo.BARISTA.getItem(Material.PAPER, name, false, false); }
        public boolean isFunctioning() { return true; }
    };

    public enum Effect {
        NONE,
        DRUNK, POISONED, SOBER_AND_HEALTHY,
        HAS_ABILITY, NO_ABILITY,
        SAFE_FROM_DEMON, CANNOT_DIE
    }

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
        return source.getRoleInfo().getItem(Material.PAPER, name, false, source.isImpaired());
    }

    public boolean isFunctioning() {
        return !source.isImpaired();
    }

    public Effect getEffect() {
        return isFunctioning() ? effect : Effect.NONE;
    }
}
