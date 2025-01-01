package io.github.maliciousfiles.bloodOnTheClocktower.play;

import ca.spottedleaf.concurrentutil.completable.Completable;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.C;

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
                () -> mcPlayer.sendActionBar(Component.text(instruction, INSTRUCTION_COLOR, TextDecoration.BOLD)),
                0, 20);

        Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                cancel.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            task.cancel();
        });

        instructions.add(cancel);
        return cancel;
    }
    public void giveInfo(Component info) {
        mcPlayer.sendMessage(info);
    }

    private static final List<CompletableFuture<Void>> instructions = new ArrayList<>();
    public static void destruct() {
        instructions.forEach(c->c.complete(null));
    }
}
