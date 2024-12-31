package io.github.maliciousfiles.bloodOnTheClocktower;

import io.github.maliciousfiles.bloodOnTheClocktower.commands.BOTCCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.commands.SeatsCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.commands.StartGameCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Role;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.RoleInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.ScriptInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.play.*;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

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
        registerCommand("botc-game", new StartGameCommand());

        PacketManager.register();
        GrabBag.register();
        Bukkit.getPluginManager().registerEvents(new ResourcePackHandler(), this);
    }

    @Override
    public void onDisable() {
        StartGameCommand.destruct();
        PacketManager.unload();
        SeatList.destruct();
        PlayerWrapper.destruct();
    }
}
