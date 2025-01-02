package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BOTCPlayer extends PlayerWrapper {
    private RoleInfo roleInfo;
    private Role role;
    private Game game;
    private boolean alive = true;

    public final List<ReminderToken> reminderTokensOnMe = new ArrayList<>();

    public BOTCPlayer(Player mcPlayer) {
        super(mcPlayer);
    }
    public void setRole(RoleInfo role) {
        this.roleInfo = role;
    }
    public void setGame(Game game) {
        this.role = roleInfo.getInstance(this, game);
        this.game = game;
    }
    public Role getRole() { return role; }

    public void wake() {
        // TODO: Storyteller#prompt for player to wake up, with info on who they are, and make wake
    }

    public void sleep() {
        // TODO
    }

    public boolean isImpaired() {
        boolean isImpaired = false;
        for (ReminderToken token : reminderTokensOnMe) {
            if (token.effect == ReminderToken.Effect.DRUNK || token.effect == ReminderToken.Effect.POISONED) {
                if (token.source == this || token.isFunctioning()) {
                    isImpaired = true;
                }
            } else if (token.getEffect() == ReminderToken.Effect.SOBER_AND_HEALTHY) {
                return false;
            }
        }
        return isImpaired;
    }

    public void die() {
        // TODO
        alive = false;
        game.checkVictory();
    }

    public void switchRole(RoleInfo newRole) {
        role.handleRoleSwitch();
        setRole(newRole);
        role = roleInfo.getInstance(this, game);
        // TODO: tell new role and do new role setup
    }

    public enum DeathCause { STORY, EXECUTION, PLAYER }
    public void handleDeathAttempt(DeathCause cause, BOTCPlayer killer) throws ExecutionException, InterruptedException {
        role.handleDeathAttempt(cause, killer);
    }

    public boolean isAlive() { return alive; }
}
