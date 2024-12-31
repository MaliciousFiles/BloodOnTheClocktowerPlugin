package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// TODO: maybe remove
public class PacketManager implements Listener {
    private static final Map<Class<? extends Packet<?>>, List<BiConsumer<Player, ?>>> listeners = new HashMap<>();

    public static <T extends Packet<?>> void registerListener(Class<T> packetClass, BiConsumer<Player, T> listener) {
        listeners.computeIfAbsent(packetClass, k -> new ArrayList<>()).add(listener);
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
        pipeline.addBefore("packet_handler", "botc_packet_listener", new SimpleChannelInboundHandler<Packet<?>>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Packet<?> packet) {
                ctx.fireChannelRead(packet);
                listeners.getOrDefault(packet.getClass(), new ArrayList<>())
                        .forEach(listener -> ((BiConsumer<Player, Packet<?>>) listener).accept(player, packet));
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
