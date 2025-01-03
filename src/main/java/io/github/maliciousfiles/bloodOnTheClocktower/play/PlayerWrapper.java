package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class PlayerWrapper {
    private final Player mcPlayer;

    public PlayerWrapper(Player mcPlayer) {
        this.mcPlayer = mcPlayer;
    }

    public void setTeam(PlayerTeam team) {
        ((CraftPlayer) mcPlayer).getHandle().connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false));
        ((CraftPlayer) mcPlayer).getHandle().connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
    }

    public Player getPlayer() {
        return mcPlayer;
    }
    public String getName() {
        return mcPlayer.getName();
    }

    private int activeId = 0;

    private CompletableFuture<Void> message(Component message) {
        CompletableFuture<Void> cancel = new CompletableFuture<>();

        final int id = activeId += 1;

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(BloodOnTheClocktower.instance,
                () -> { if (activeId == id) mcPlayer.sendActionBar(message); }, 0, 20);

        Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                activeId--;
                cancel.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            task.cancel();
        });

        futures.add(cancel);
        return cancel;

    }

    public static final TextColor INSTRUCTION_COLOR = TextColor.color(123, 236, 255);
    public CompletableFuture<Void> giveInstruction(String instruction) {
        return message(Component.text(instruction, INSTRUCTION_COLOR));
    }
    public static final TextColor QUESTION_COLOR = TextColor.color(255, 162, 87);
    public CompletableFuture<Void> askQuestion(String question) {
        return message(Component.text(question, QUESTION_COLOR));
    }

    public void giveInfo(Component info) {
        mcPlayer.sendMessage(info);
    }

    private static final List<CompletableFuture<Void>> futures = new ArrayList<>();
    public static void destruct() {
        futures.forEach(c->c.complete(null));
    }

    private final List<CompletableFuture<Void>> deglow = new ArrayList<>();
    public void glow(List<PlayerWrapper> recipients) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        Packet<?> glow = new ClientboundSetEntityDataPacket(mcPlayer.getEntityId(), List.of(SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(0), (byte) 0x40)));
        recipients.forEach(p -> ((CraftPlayer) p.mcPlayer).getHandle().connection.send(glow));

        Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                future.get();
                recipients.forEach(p -> ((CraftPlayer) mcPlayer).getHandle()
                        .refreshEntityData(((CraftPlayer) p.mcPlayer).getHandle()));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        futures.add(future);
        deglow.add(future);
    }

    public void deglow() {
        deglow.forEach(f->f.complete(null));
        deglow.clear();
    }

    public void kill() {
        mcPlayer.setPose(Pose.DYING);
        Bukkit.getScheduler().runTaskLater(BloodOnTheClocktower.instance, () -> {
            mcPlayer.setInvisible(true);
        }, 10);
    }
}
