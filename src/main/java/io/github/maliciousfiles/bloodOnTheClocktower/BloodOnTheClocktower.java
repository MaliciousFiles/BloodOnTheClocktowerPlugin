package io.github.maliciousfiles.bloodOnTheClocktower;

import io.github.maliciousfiles.bloodOnTheClocktower.commands.BOTCCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.commands.SeatsCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.commands.StartGameCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.ScriptInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.play.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public final class BloodOnTheClocktower extends JavaPlugin {
    public static BloodOnTheClocktower instance;

    private void registerCommand(String command, BOTCCommand handler) {
        getCommand(command).setExecutor(handler);
        getCommand(command).setTabCompleter(handler);
    }

    public static ItemStack createItem(Material material, Consumer<ItemMeta> metaConsumer) {
        ItemStack item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();
        metaConsumer.accept(meta);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void onEnable() {
        instance = this;

        ConfigurationSerialization.registerClass(ScriptInfo.class);

        registerCommand("botc-seats", new SeatsCommand());
        registerCommand("botc-game", new StartGameCommand());

        PacketManager.register();
        GrabBag.register();
        Grimoire.register();
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
