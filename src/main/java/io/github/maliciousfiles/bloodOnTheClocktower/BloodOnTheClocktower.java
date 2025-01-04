package io.github.maliciousfiles.bloodOnTheClocktower;

import io.github.maliciousfiles.bloodOnTheClocktower.commands.BOTCCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.commands.SeatsCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.commands.StartGameCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.ScriptInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.play.*;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Pose;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class BloodOnTheClocktower extends JavaPlugin {
    public static BloodOnTheClocktower instance;

    public static NamespacedKey key(String key) {
        return new NamespacedKey("botc", key);
    }

    private void registerCommand(String command, BOTCCommand handler) {
        getCommand(command).setExecutor(handler);
        getCommand(command).setTabCompleter(handler);
    }

    public static ItemStack createItem(Material material, DataComponentPair<?>... data) {
        ItemStack item = ItemStack.of(material);
        for (DataComponentPair<?> pair : data) item = pair.apply(item);
        return item;
    }

    public static ComponentLogger logger() {
        return instance.getComponentLogger();
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
        MiscGameHandler.register();
        Bukkit.getPluginManager().registerEvents(new ResourcePackHandler(), this);
    }

    @Override
    public void onDisable() {
        StartGameCommand.destruct();
        PacketManager.unload();
        SeatList.destruct();
        PlayerWrapper.destruct();
        ChoppingBlock.destruct();
        Game.destruct();
    }
}
