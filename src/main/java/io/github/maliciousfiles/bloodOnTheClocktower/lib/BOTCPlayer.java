package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BOTCPlayer extends PlayerWrapper {
    private final Role role;

    private final List<StatusEffect> statusEffects = new ArrayList<>();

    public BOTCPlayer(Player mcPlayer, Role role, Game game) {
        super(mcPlayer);
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void wake() {
        // TODO: Storyteller#prompt for player to wake up, with info on who they are, and make wake
    }

    public void sleep() {
        // TODO
    }

    public void onDusk() { // happens at start of night
        statusEffects.removeIf(StatusEffect::tickDusk);
    }

    public void onDawn() {
        statusEffects.removeIf(StatusEffect::tickDawn);
    }

    public void addStatusEffect(StatusEffect effect) {
        statusEffects.add(effect);
    }

    public boolean isMalfunctioning() {
        return statusEffects.stream().anyMatch(e -> e.type == StatusEffect.EffectType.DRUNK ||
                e.type == StatusEffect.EffectType.POISONED);
    }
}
