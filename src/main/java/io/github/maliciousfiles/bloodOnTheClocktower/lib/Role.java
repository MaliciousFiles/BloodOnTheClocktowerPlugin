package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;

public abstract class Role {
    public enum Type { TOWNSFOLK, OUTSIDER, MINION, DEMON, TRAVELLER, FABLED }

    public final RoleInfo info;

    protected final BOTCPlayer me;
    protected final Game game;

    public Role(BOTCPlayer me, Game game, RoleInfo info) {
        this.me = me;
        this.game = game;
        this.info = info;
    }

    public void setup() {}
    public void handleNight() throws InterruptedException, ExecutionException {}

    public enum DeathCause { STORY, EXECUTION, PLAYER }
    public void handleDeathAttempt(DeathCause cause, @Nullable BOTCPlayer killer) {
        me.die();
    }
}
