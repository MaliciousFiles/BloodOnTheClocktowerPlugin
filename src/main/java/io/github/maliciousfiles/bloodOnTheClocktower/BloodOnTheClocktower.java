package io.github.maliciousfiles.bloodOnTheClocktower;

import io.github.maliciousfiles.bloodOnTheClocktower.commands.BOTCCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.commands.SeatsCommand;
import io.github.maliciousfiles.bloodOnTheClocktower.play.GrabBag;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PacketManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class BloodOnTheClocktower extends JavaPlugin {
    public static BloodOnTheClocktower instance;

    private void registerCommand(String command, BOTCCommand handler) {
        getCommand(command).setExecutor(handler);
        getCommand(command).setTabCompleter(handler);
    }

    @Override
    public void onEnable() {
        instance = this;

        registerCommand("botc-seats", new SeatsCommand());

        PacketManager.register();
        GrabBag.register();

        getCommand("test").setExecutor((sender, _, _, _) -> {
            if (sender instanceof Player player) {
                player.getInventory().addItem(GrabBag.createGrabBag(meta -> {
                    meta.displayName(Component.text("Grab Bag")
                            .decoration(TextDecoration.ITALIC, false));
                }, false, false, new ItemStack(Material.DIAMOND), new ItemStack(Material.EMERALD), new ItemStack(Material.GOLD_INGOT), new ItemStack(Material.IRON_INGOT), new ItemStack(Material.COPPER_INGOT), new ItemStack(Material.GOLD_NUGGET), new ItemStack(Material.IRON_NUGGET), new ItemStack(Material.DIAMOND_BLOCK)));
            }
            return true;
        });
    }

    @Override
    public void onDisable() {
        PacketManager.unload();
    }
}
