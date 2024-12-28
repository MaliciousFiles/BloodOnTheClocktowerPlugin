package io.github.maliciousfiles.bloodOnTheClocktower;

import io.github.maliciousfiles.bloodOnTheClocktower.commands.BOTCCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.commands.SeatsCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Role;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.RoleInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.ScriptInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.play.GrabBag;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PacketManager;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ResourcePackHandler;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ScriptDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class BloodOnTheClocktower extends JavaPlugin {
    public static BloodOnTheClocktower instance;

    private void registerCommand(String command, BOTCCommand handler) {
        getCommand(command).setExecutor(handler);
        getCommand(command).setTabCompleter(handler);
    }

    @Override
    public void onEnable() {
        instance = this;

        ConfigurationSerialization.registerClass(ScriptInfo.class);

        registerCommand("botc-seats", new SeatsCommand());

        PacketManager.register();
        GrabBag.register();
        Bukkit.getPluginManager().registerEvents(new ResourcePackHandler(), this);

        getCommand("test").setExecutor((sender, _, _, _) -> {
            if (sender instanceof Player player) {
                CompletableFuture<ScriptInfo> wait = new CompletableFuture<>();
                ScriptDisplay.open(player, wait);

                new Thread(() -> {
                    ScriptInfo script;
                    try {
                        script = wait.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                    player.getInventory().addItem(GrabBag.createGrabBag(meta -> {
                        meta.displayName(Component.text(script.name)
                                .decoration(TextDecoration.ITALIC, false));
                    }, false, false, script.roles.stream()
                            .filter(r->r.type() != Role.Type.TRAVELLER && r.type() != Role.Type.FABLED)
                            .map(RoleInfo::getItem).toList()));
                }).start();
            }
            return true;
        });
    }

    @Override
    public void onDisable() {
        PacketManager.unload();
    }
}
