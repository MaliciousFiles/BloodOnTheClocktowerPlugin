package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.play.*;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.AnvilDropHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
    private boolean shouldDie;

    public final List<ReminderToken> reminderTokensOnMe = new ArrayList<>();

    public BOTCPlayer(@NotNull Player mcPlayer) {
        super(mcPlayer);
    }
    public void setRole(@NotNull RoleInfo role) {
        this.roleInfo = role;
        this.role = roleInfo.getInstance(this, game);
    }
    public void setAlignment(@NotNull Alignment alignment) {
        this.alignment = alignment;
    }
    public void setGame(@NotNull Game game) {
        this.game = game;
        VIEW_SCRIPT.enable(() -> ScriptDisplay.viewRoles(getPlayer(), game.getScript(), 0, Component.text(game.getScript().name)));
    }
    public RoleInfo getRoleInfo() { return roleInfo; }
    public Alignment getAlignment() { return alignment; }
    public List<ReminderToken> getMyReminderTokens() { return new ArrayList<>(role.myReminderTokens); }
    public void moveReminderToken(ReminderToken token, BOTCPlayer target) { role.moveReminderToken(token, target); }

    @Override
    public void setupInventory() {
        super.setupInventory();
        VOTE.disable();
        VIEW_SCRIPT.disable();
    }

    public boolean isAwake() {
        return awake;
    }

    public void useDeadVote() {
        deadVote = false;
        VOTE.remove();
        game.log("used dead vote", this, Game.LogPriority.LOW);
    }
    public void returnDeadVote() {
        deadVote = true;
        VOTE.disable();
    }
    public boolean hasDeadVote() {
        return deadVote;
    }

    private Runnable allowSounds1, allowSounds2;
    public void wake() {
        game.getPlayers().forEach(p -> {
            if (p == this) return;
            ((CraftPlayer) p.getPlayer()).getHandle().moonrise$getTrackedEntity().serverEntity.sendPairingData(((CraftPlayer) getPlayer()).getHandle(), ((CraftPlayer) getPlayer()).getHandle().connection::send);
        });
        getPlayer().sendPotionEffectChangeRemove(getPlayer(), PotionEffectType.BLINDNESS);
        if (allowSounds1 != null) {
            allowSounds1.run();
            allowSounds2.run();
            allowSounds1 = allowSounds2 = null;
        }

        awake = true;
        game.getSeats().setCanStand(this, true);
        game.log("woke up", this, Game.LogPriority.LOW);
    }

    public void sleep() {
        game.getSeats().forceSit(this);

        getPlayer().sendPotionEffectChange(getPlayer(), new PotionEffect(PotionEffectType.DARKNESS, PotionEffect.INFINITE_DURATION, 255, true, false, false));

        Bukkit.getScheduler().runTaskLater(BloodOnTheClocktower.instance, () -> {
            getPlayer().sendPotionEffectChangeRemove(getPlayer(), PotionEffectType.DARKNESS);
            if (awake) return;

            getPlayer().sendPotionEffectChange(getPlayer(), new PotionEffect(PotionEffectType.BLINDNESS, PotionEffect.INFINITE_DURATION, 255, true, false, false));

            ((CraftPlayer) getPlayer()).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(
                    game.getPlayers().stream().filter(p->p!=this).mapToInt(p->p.getPlayer().getEntityId()).toArray()));

            allowSounds1 = PacketManager.registerListener(ClientboundSoundPacket.class, (player, _) -> player == getPlayer());
            allowSounds2 = PacketManager.registerListener(ClientboundSoundEntityPacket.class, (player, _) -> player == getPlayer());
        }, 15);

        awake = false;

        game.getSeats().setCanStand(this, false);
        game.log("went to sleep", this, Game.LogPriority.LOW);
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

    public void revive() throws ExecutionException, InterruptedException {
        alive = true;
        returnDeadVote();
        game.log("revived", this, Game.LogPriority.MEDIUM);
        getPlayer().setInvisible(false);

        role.setup();
    }

    public void visuallyDie() {
        game.getPlayers().forEach(p -> {
            if (p == this) return;

            ((CraftPlayer) p.getPlayer()).getHandle().connection.send(new ClientboundSetEntityDataPacket(getPlayer().getEntityId(),
                    List.of(SynchedEntityData.DataValue.create(EntityDataSerializers.FLOAT.createAccessor(9), 0f))));
        });
        Bukkit.getScheduler().runTaskLater(BloodOnTheClocktower.instance, () -> {
            getPlayer().setInvisible(true);

            game.getPlayers().forEach(p -> {
                ((CraftPlayer) p.getPlayer()).getHandle().connection.send(new ClientboundEntityEventPacket(((CraftPlayer) getPlayer()).getHandle(), (byte) 60));

                if (p == this) return;
                ((CraftPlayer) p.getPlayer()).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(getPlayer().getEntityId()));
                ((CraftPlayer) getPlayer()).getHandle().moonrise$getTrackedEntity().serverEntity.sendPairingData(((CraftPlayer) p.getPlayer()).getHandle(), ((CraftPlayer) p.getPlayer()).getHandle().connection::send);
            });
        }, 20);

        shouldDie = false;
    }

    public void die() {
        if (!game.isNight()) visuallyDie();
        else shouldDie = true;

        alive = false;
        game.log("died", this, Game.LogPriority.MEDIUM);
        game.checkVictory();
    }

    public void updateRoleItem() {
        for (int i = 0; i < getPlayer().getInventory().getSize(); i++) {
            if (RoleInfo.isRoleItem(getPlayer().getInventory().getItem(i))) {
                getPlayer().getInventory().setItem(i, roleInfo.getItem(isImpaired()));
                return;
            }
        }
    }
    public void changeRoleAndAlignment(@Nullable RoleInfo newRole, @Nullable Alignment newAlignment) throws ExecutionException, InterruptedException {
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
        game.log("{0} became the " + newAlignment + " " + newRole.title(), null, Game.LogPriority.MEDIUM, this);

        role.setup();
    }

    public boolean isAlive() { return alive; }
    public boolean shouldDie() { return shouldDie; }

    public boolean hasAbility() { return role.hasAbility(); }
    public boolean countsAsAlive() { return role.countsAsAlive(); }
    public boolean blocksGoodVictory() { return role.blocksGoodVictory(); }
    public void handleDusk() { role.handleDusk(); }
    public void handleDawn() { role.handleDawn(); }
    public void setup() throws ExecutionException, InterruptedException { role.setup(); }
    public Collection<Game.NightAction> getNightActions() { return role.getNightActions(); }

    public void handleDeathAttempt(DeathCause cause, @Nullable BOTCPlayer killer) throws ExecutionException, InterruptedException {
        if (killer == null) {
            game.log("death attempt from " + cause, this, Game.LogPriority.LOW);
        } else {
            game.log("death attempt from {0}", this, Game.LogPriority.LOW, killer);
        }

        role.handleDeathAttempt(cause, killer);
    }

    public void execute(boolean force) throws ExecutionException, InterruptedException {
        new AnvilDropHook(getPlayer().getLocation().add(0, 5, 0)).get();

        Bukkit.getScheduler().callSyncMethod(BloodOnTheClocktower.instance, () -> {
            if (force) die();
            else handleDeathAttempt(BOTCPlayer.DeathCause.EXECUTION, null);
            return null; // it wants a return type >:c
        }).get();
    }
}
