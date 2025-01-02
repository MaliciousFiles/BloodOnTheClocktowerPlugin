package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BOTCPlayer extends PlayerWrapper {
    public enum DeathCause { STORY, EXECUTION, PLAYER }
    public enum Alignment { GOOD, EVIL }

    private RoleInfo roleInfo;
    private Role role;
    private Alignment alignment;
    private Game game;
    private boolean alive = true;

    public final List<ReminderToken> reminderTokensOnMe = new ArrayList<>();

    public BOTCPlayer(@NotNull Player mcPlayer) {
        super(mcPlayer);
    }
    public void setRole(@NotNull RoleInfo role) {
        this.roleInfo = role;
        if (game != null) {
            this.role = roleInfo.getInstance(this, game);
        }
    }
    public void setAlignment(@NotNull Alignment alignment) {
        this.alignment = alignment;
    }
    public void setGame(@NotNull Game game) {
        this.game = game;
        setRole(roleInfo);
    }
    public Role getRole() { return role; }
    public Alignment getAlignment() { return alignment; }

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

    public void changeRoleAndAlignment(@Nullable RoleInfo newRole, @Nullable Alignment newAlignment) {
        if (newRole == null) { newRole = roleInfo; }
        if (newAlignment == null) { newAlignment = alignment; }

        if ((roleInfo.type() == Role.Type.TRAVELLER) != (newRole.type() == Role.Type.TRAVELLER)) {
            game.getStoryteller().giveInfo(Component.text("A traveller cannot become a non-traveller, or vice versa!", NamedTextColor.RED));
            return;
        }

        role.handleRoleChange();
        setRole(newRole);
        setAlignment(newAlignment);
        role = roleInfo.getInstance(this, game);
        // TODO: tell new role/alignment and do new role setup
    }

    public boolean isAlive() { return alive; }
}
