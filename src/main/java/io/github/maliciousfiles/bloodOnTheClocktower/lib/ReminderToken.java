package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import javax.annotation.Nullable;

public class ReminderToken {
    public enum Effect { NONE, DRUNK, POISONED }

    public final String name;
    @Nullable public final BOTCPlayer source;
    public final Effect effect;
    @Nullable public BOTCPlayer target;

    public ReminderToken(String name, BOTCPlayer source, @Nullable BOTCPlayer target, Effect effect) {
        this.name = name;
        this.source = source;
        this.target = target;
        this.effect = effect;
    }
}
