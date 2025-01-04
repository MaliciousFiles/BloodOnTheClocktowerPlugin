package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.netty.channel.*;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PacketManager implements Listener {
    private static final Map<Class<? extends Packet<?>>, Map<UUID, BiFunction<Player, ? extends Packet<?>, Boolean>>> listeners = new HashMap<>();

    public static <T extends Packet<?>> Runnable registerListener(Class<T> packetClass, BiFunction<Player, T, Boolean> listener) {
        UUID uuid = UUID.randomUUID();

        listeners.computeIfAbsent(packetClass, _ -> new HashMap<>()).put(uuid,listener);
        return () -> listeners.get(packetClass).remove(uuid);
    }
    public static <T extends Packet<?>> Runnable registerListener(Class<T> packetClass, BiConsumer<Player, T> listener) {
        UUID uuid = UUID.randomUUID();

        listeners.computeIfAbsent(packetClass, _ -> new HashMap<>()).put(uuid,(Player p, T pa) -> {
            listener.accept(p, pa);
            return false;
        });
        return () -> listeners.get(packetClass).remove(uuid);
    }

    private static List<Runnable> unloadTasks = new ArrayList<>();
    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        handlePlayer(evt.getPlayer());
    }

    private static void handlePlayer(Player player) {
        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline();

        if (pipeline.names().contains("botc_packet_listener")) {
            pipeline.remove("botc_packet_listener");
        }
        pipeline.addBefore("packet_handler", "botc_packet_listener", new ChannelDuplexHandler() {
            private boolean handleMessage(Object msg) {
                if (!(msg instanceof Packet<?> packet)) return false;

                for (BiFunction<Player, ? extends Packet<?>, Boolean> listener : listeners.getOrDefault(packet.getClass(), new HashMap<>()).values()) {
                    if (((BiFunction<Player, Packet<?>, Boolean>) listener).apply(player, packet)) return true;
                }

                return false;
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                if (handleMessage(msg)) return;
                ctx.fireChannelRead(msg);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (handleMessage(msg)) return;
                super.write(ctx, msg, promise);
            }
        });

        unloadTasks.add(() -> {
            if (pipeline.channel().isActive()) pipeline.remove("botc_packet_listener");
        });
    }

    public static void register() {
        BloodOnTheClocktower.instance.getServer().getPluginManager().registerEvents(new PacketManager(), BloodOnTheClocktower.instance);
        Bukkit.getOnlinePlayers().forEach(PacketManager::handlePlayer);
    }
    public static void unload() {
        unloadTasks.forEach(Runnable::run);
        unloadTasks.clear();
    }
}
