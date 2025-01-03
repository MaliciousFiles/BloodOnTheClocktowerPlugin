package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class PlayerWrapper {
    private final Player mcPlayer;

    public PlayerWrapper(Player mcPlayer) {
        this.mcPlayer = mcPlayer;
    }

    public Player getPlayer() {
        return mcPlayer;
    }
    public String getName() {
        return mcPlayer.getName();
    }

    public static final TextColor INSTRUCTION_COLOR = TextColor.color(123, 236, 255);
    public CompletableFuture<Void> giveInstruction(String instruction) {
        CompletableFuture<Void> cancel = new CompletableFuture<>();

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(BloodOnTheClocktower.instance,
                () -> mcPlayer.sendActionBar(Component.text(instruction, INSTRUCTION_COLOR)),
                0, 20);

        Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                cancel.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            task.cancel();
        });

        futures.add(cancel);
        return cancel;
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

        SynchedEntityData data = ((CraftPlayer) mcPlayer).getHandle().getEntityData();
        data.set(EntityDataSerializers.BYTE.createAccessor(0), (byte) 0x40);
        Packet<?> glow = new ClientboundSetEntityDataPacket(mcPlayer.getEntityId(), data.packAll());
        recipients.forEach(p -> ((CraftPlayer) p.mcPlayer).getHandle().connection.send(glow));

        Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                recipients.forEach(p -> ((CraftPlayer) mcPlayer).getHandle()
                        .refreshEntityData(((CraftPlayer) p.mcPlayer).getHandle()));
                future.get();
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
}
