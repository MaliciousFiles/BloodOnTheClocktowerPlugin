package io.github.maliciousfiles.bloodOnTheClocktower.lib;

public class ReminderToken {
    enum Effect { NONE, DRUNK, POISONED }

    BOTCPlayer source;
    BOTCPlayer target;
    Effect effect;
}
