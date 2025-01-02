package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.netty.channel.*;
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

public class PacketManager implements Listener {
    private static final Map<Class<? extends Packet<?>>, List<BiConsumer<Player, ? extends Packet<?>>>> listeners = new HashMap<>();

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
        pipeline.addBefore("packet_handler", "botc_packet_listener", new ChannelDuplexHandler() {
            private void handleMessage(Object msg) {
                if (!(msg instanceof Packet<?> packet)) return;

                listeners.getOrDefault(packet.getClass(), new ArrayList<>())
                        .forEach(listener -> ((BiConsumer<Player, Packet<?>>) listener).accept(player, packet));
            }

            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                handleMessage(msg);
                ctx.fireChannelRead(msg);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                handleMessage(msg);
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
