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
    protected void removeAllReminderTokens() {
        for (ReminderToken token : myReminderTokens) {
            moveReminderToken(token, null);
        }
        myReminderTokens.clear();
    }

    public boolean hasAbility() {
        return me.isAlive() || me.reminderTokensOnMe.stream().anyMatch(tok -> tok.getEffect() == ReminderToken.Effect.HAS_ABILITY);
    }
    protected abstract boolean hasNightAction();
    public final boolean shouldRunNight() {
        return hasAbility() && hasNightAction();
    }

    public void setup() throws ExecutionException, InterruptedException {}
    public void handleRoleChange() {
        removeAllReminderTokens();
    }
    public void handleDusk() {}
    public void handleDawn() {}
    public void handleNight() throws InterruptedException, ExecutionException {}

    public void handleDeathAttempt(BOTCPlayer.DeathCause cause, @Nullable BOTCPlayer killer) throws ExecutionException, InterruptedException {
        if (me.isAlive()) {
            me.die();
            removeAllReminderTokens();
        }
    }

    public class RoleNightAction implements Game.NightAction {
        @Override
        public String name() { return "night action for " + info.title(); }

        @Override
        public boolean shouldRun() { return shouldRunNight(); }

        @Override
        public float order() { return info.nightOrder(); }

        @Override
        public void run() throws ExecutionException, InterruptedException {
            handleNight();
            me.sleep();
        }
    }

    public List<Game.NightAction> getNightActions() {
        return List.of(new RoleNightAction());
    }

    public boolean countsAsAlive() {
        return me.isAlive() && info.type() != Type.TRAVELLER;
    }

    public boolean blocksGoodVictory() { return false; }
}
