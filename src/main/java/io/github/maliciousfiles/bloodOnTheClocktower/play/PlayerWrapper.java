package io.github.maliciousfiles.bloodOnTheClocktower.play;

import com.google.common.collect.Lists;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.util.EntityHolder;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class PlayerWrapper {
    private ItemStack[] inventory;
    private final EntityHolder<Player> mcPlayer;

    public PlayerWrapper(Player mcPlayer) {
        this.mcPlayer = new EntityHolder<>(mcPlayer);
    }

    public void setupInventory() {
        inventory = getPlayer().getInventory().getContents();
        getPlayer().getInventory().clear();
    }

    public void resetInventory() {
        getPlayer().getInventory().setContents(inventory);
    }

    public void setTeam(PlayerTeam team) {
        ((CraftPlayer) getPlayer()).getHandle().connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false));
        ((CraftPlayer) getPlayer()).getHandle().connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
    }

    public Player getPlayer() {
        return mcPlayer.get();
    }
    public String getName() {
        return getPlayer().getName();
    }

    private final List<CompletableFuture<Void>> messages = new ArrayList<>();
    private int activeId = 0;
    private CompletableFuture<Void> message(Component message) {
        CompletableFuture<Void> cancel = new CompletableFuture<>();

        final int id = activeId += 1;
        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(BloodOnTheClocktower.instance,
                () -> { if (activeId == id) getPlayer().sendActionBar(message); }, 0, 20);

        Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                cancel.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            activeId--;
            task.cancel();
        });

        messages.add(cancel);
        futures.add(cancel);
        return cancel;
    }

    public void clearInstructions() {
        messages.forEach(c->c.complete(null));
        messages.clear();
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
        getPlayer().sendMessage(info);
    }

    private static final List<CompletableFuture<Void>> futures = new ArrayList<>();
    public static void destruct() {
        futures.forEach(c->c.complete(null));
    }

    private final List<CompletableFuture<Void>> deglow = new ArrayList<>();
    public void glow(List<PlayerWrapper> recipients) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        byte value = (byte) (getPlayer().isInvisible() ? 0x60 : 0x40);
        Packet<?> glow = new ClientboundSetEntityDataPacket(getPlayer().getEntityId(), Lists.newArrayList(SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(0), value)));
        recipients.forEach(p -> ((CraftPlayer) p.getPlayer()).getHandle().connection.send(glow));

        Runnable unregister = PacketManager.registerListener(ClientboundSetEntityDataPacket.class, (player, packet) -> {
            if (!player.equals(mcPlayer) || recipients.stream().noneMatch(p->p.getPlayer().equals(player))) return;

            for (int i = 0; i < packet.packedItems().size(); i++) {
                SynchedEntityData.DataValue<?> val = packet.packedItems().get(i);
                if (val.value() instanceof Byte && val.id() == 0) {
                    packet.packedItems().set(i,
                            SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(0), value));
                }
            }
        });

        Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                future.get();
                unregister.run();
                recipients.forEach(p ->
                        ((CraftPlayer) p.getPlayer()).getHandle().connection.send(new ClientboundSetEntityDataPacket(getPlayer().getEntityId(), ((CraftPlayer) getPlayer()).getHandle().getEntityData().packAll())));
                futures.remove(future);
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
