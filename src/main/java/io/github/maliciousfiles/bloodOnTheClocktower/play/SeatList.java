package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import net.kyori.adventure.text.Component;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SeatList {
    private final List<Seat> seats;

    private SeatList(List<Location> locs) {
        seats = new ArrayList<>();
        for (Location location : locs) {
            if (location == null) {
                seats.add(null);
                continue;
            }

            Seat seat = new Seat(location);
            seats.add(seat);
            Bukkit.getPluginManager().registerEvents(seat, BloodOnTheClocktower.instance);
        }}

    public static SeatList initSeats(List<Location> locs) {
        assert Thread.currentThread() == MinecraftServer.getServer().getRunningThread(); // must be called sync

        return new SeatList(locs);
    }

    public CompletableFuture<Void> selectSeats(List<Player> players, Consumer<Player> onSit) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        List<Player> sitting = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat == null) continue;

            seat.whitelist = players;
            seat.canSit = true;
            seat.textDisplay.text(Component.text("Right click to sit down"));

            seat.onSit = player -> {
                onSit.accept(player);

                seat.onSit = null;
                sitting.add(player);
                if (sitting.size() == players.size()) {
                    for (Seat s : seats) {
                        if (s.owner != null) continue;
                        s.canSit = false;
                        s.textDisplay.remove();
                        s.interaction.remove();
                    }
                    future.complete(null);
                }
            };
        }

        return future;
    }

    public List<Player> getSeatOrder() {
        return seats.stream().map(s->s == null ? null : s.owner).toList();
    }

    public void setCanStand(BOTCPlayer player, boolean canStand) {
        for (Seat seat : seats) {
            if (seat == null || seat.owner == null || !seat.owner.equals(player.getPlayer())) continue;
            seat.canStand = canStand;
        }
    }


    private static final List<Entity> spawnedEntities = new ArrayList<>();
    public static void destruct() {
        spawnedEntities.forEach(Entity::remove);
    }
    private class Seat implements Listener {
        private final Interaction interaction;
        private final TextDisplay textDisplay;

        private boolean canSit, canStand, occupied;
        @Nullable  private List<Player> whitelist;
        @Nullable private Player owner;
        @Nullable private Consumer<Player> onSit;

        // TODO: handle stairs
        public Seat(Location location) {
            interaction = (Interaction) location.getWorld().spawnEntity(location.add(0.5f, 0, 0.5f), EntityType.INTERACTION);
            interaction.setInteractionWidth(1.05f);
            interaction.setInteractionHeight((float) location.getBlock().getBoundingBox().getHeight()+0.05f);

            textDisplay = (TextDisplay) location.getWorld().spawnEntity(location.add(0, interaction.getInteractionHeight()+0.5, 0), EntityType.TEXT_DISPLAY);
            textDisplay.setSeeThrough(true);
            textDisplay.setBillboard(Display.Billboard.CENTER);

            spawnedEntities.add(interaction);
            spawnedEntities.add(textDisplay);
        }

        @EventHandler
        public void onInteract(PlayerInteractEntityEvent evt) {
            if (occupied || !canSit || !interaction.equals(evt.getRightClicked())
                    || (owner != null && !owner.equals(evt.getPlayer()))
                    || (whitelist != null && !whitelist.contains(evt.getPlayer()))) return;
            if (seats.stream().anyMatch(s -> s != this && s != null && evt.getPlayer().equals(s.owner))) return;

            if (owner == null) owner = evt.getPlayer();
            if (onSit != null) onSit.accept(owner);

            occupied = true;
            interaction.addPassenger(owner);
            textDisplay.text(Component.empty());
        }

        @EventHandler
        public void onSneak(PlayerToggleSneakEvent evt) {
            if (!occupied || !evt.isSneaking() || !evt.getPlayer().equals(owner)) return;
            if (!canStand) {
                evt.setCancelled(true);
                return;
            }

            occupied = false;
            textDisplay.text(Component.text((owner.getName()+"'s Seat").replace("s's", "s'")));
        }
    }
}
