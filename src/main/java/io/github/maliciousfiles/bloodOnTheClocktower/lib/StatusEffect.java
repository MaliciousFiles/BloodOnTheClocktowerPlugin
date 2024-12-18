package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import javax.annotation.Nullable;

public class StatusEffect {
    public enum EffectType {
        POISONED,
        DRUNK,
    }
    public enum EndsOn { NIGHT, DAY, NEVER }

    public final EffectType type;
    public final BOTCPlayer source;
    public final EndsOn endsOn;
    private int duration;

    public StatusEffect(EffectType type, int duration, EndsOn endsOn, @Nullable BOTCPlayer source) {
        this.type = type;
        this.source = source;
        this.endsOn = endsOn;
        this.duration = duration;
    }

    public boolean tick() {
        return switch (endsOn) {
            case NIGHT -> duration-- == 0;
            case DAY -> --duration == 0;
            case NEVER -> false;
        };
    }
}
