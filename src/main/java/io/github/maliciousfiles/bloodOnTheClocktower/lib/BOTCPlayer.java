package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.play.ChatComponents;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerAction;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ScriptDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Pose;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class BOTCPlayer extends PlayerWrapper {

    public enum DeathCause { STORY, EXECUTION, PLAYER }
    public enum Alignment { GOOD, EVIL }

    public final PlayerAction VIEW_SCRIPT = new PlayerAction(getPlayer(),
            "View Script", TextColor.color(188, 46, 69),
            "Hold to vote for the nominated player", NamespacedKey.minecraft("writable_book"), 8);
    public final PlayerAction VOTE = new PlayerAction(getPlayer(),
            "Vote", TextColor.color(51, 186, 255),
            "Hold to vote for the nominated player", "nominate", 7);

    private RoleInfo roleInfo;
    private Role role;
    private Alignment alignment;
    private Game game;
    private boolean alive = true;
    private boolean awake = true;
    private boolean deadVote = true;

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
        VIEW_SCRIPT.enable(() -> ScriptDisplay.viewRoles(getPlayer(), game.getScript(), 0, Component.text(game.getScript().name)));
    }
    public RoleInfo getRoleInfo() { return roleInfo; }
    public Alignment getAlignment() { return alignment; }

    public boolean isAwake() {
        return awake;
    }

    public void useDeadVote() {
        deadVote = false;
        VOTE.remove();
        game.log("{0} used their dead vote", Game.LogPriority.LOW, this);
    }
    public boolean hasDeadVote() {
        return deadVote;
    }

    public void wake() {
        // TODO: Storyteller#prompt for player to wake up, with info on who they are, and make wake
        awake = true;
        game.getSeats().setCanStand(this, true);
        game.log("{0} woke up", Game.LogPriority.LOW, this);
    }

    public void sleep() {
        // TODO
        awake = false;
        game.getSeats().setCanStand(this, false);
        game.log("{0} went to sleep", Game.LogPriority.LOW, this);
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
        game.log("{0} died", Game.LogPriority.MEDIUM, this);
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
        game.log("{0} became the " + newAlignment + " " + newRole.title(), Game.LogPriority.MEDIUM, this);
        // TODO: tell new role/alignment and do new role setup
    }

    public boolean isAlive() { return alive; }

    public boolean hasAbility() { return role.hasAbility(); }
    public boolean countsAsAlive() { return role.countsAsAlive(); }
    public boolean blocksGoodVictory() { return role.blocksGoodVictory(); }
    public void handleDusk() { role.handleDusk(); }
    public void handleDawn() { role.handleDawn(); }
    public void setup() throws ExecutionException, InterruptedException { role.setup(); }
    public Collection<Game.NightAction> getNightActions() { return role.getNightActions(); }

    public void handleDeathAttempt(DeathCause cause, @Nullable BOTCPlayer killer) throws ExecutionException, InterruptedException {
        if (killer == null) {
            game.log("{0} had a death attempt from " + cause, Game.LogPriority.LOW, this);
        } else {
            game.log("{0} had a death attempt from {1}", Game.LogPriority.LOW, this, killer);
        }

        role.handleDeathAttempt(cause, killer);
    }

    public void kill() {
        game.getPlayers().forEach(p -> {
            if (p == this) return;

            ((CraftPlayer) p.getPlayer()).getHandle().connection.send(new ClientboundSetEntityDataPacket(getPlayer().getEntityId(),
                    List.of(SynchedEntityData.DataValue.create(EntityDataSerializers.FLOAT.createAccessor(9), 0f))));
        });
        Bukkit.getScheduler().runTaskLater(BloodOnTheClocktower.instance, () -> {
            game.getPlayers().forEach(p -> {
                ((CraftPlayer) p.getPlayer()).getHandle().connection.send(new ClientboundEntityEventPacket(((CraftPlayer) getPlayer()).getHandle(), (byte) 60));
                ((CraftPlayer) p.getPlayer()).getHandle().connection.send(new ClientboundSetEntityDataPacket(getPlayer().getEntityId(),
                        List.of(SynchedEntityData.DataValue.create(EntityDataSerializers.FLOAT.createAccessor(9), (float) getPlayer().getHealth()))));
            });
            getPlayer().setInvisible(true);
        }, 20);
    }
}
