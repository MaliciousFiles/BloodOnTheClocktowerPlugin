package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class Role {
    public enum Type { TOWNSFOLK, OUTSIDER, MINION, DEMON, TRAVELLER, FABLED }

    public final RoleInfo info;

    protected final BOTCPlayer me;
    protected final Game game;

    protected final List<ReminderToken> myReminderTokens = new ArrayList<>();

    public Role(BOTCPlayer me, Game game, RoleInfo info) {
        this.me = me;
        this.game = game;
        this.info = info;
    }

    protected void newReminderToken(ReminderToken token) {
        if (token.target != null) token.target.reminderTokensOnMe.add(token);
        myReminderTokens.add(token);
    }
    protected void moveReminderToken(ReminderToken token, @Nullable BOTCPlayer newTarget) {
        if (token.target != null) token.target.reminderTokensOnMe.remove(token);
        token.target = newTarget;
        if (newTarget != null) newTarget.reminderTokensOnMe.add(token);
    }

    public void setup() {}
    public void handleNight() throws InterruptedException, ExecutionException {}

    public enum DeathCause { STORY, EXECUTION, PLAYER }
    public void handleDeathAttempt(DeathCause cause, @Nullable BOTCPlayer killer) {
        me.die();
    }
}
