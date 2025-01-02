package io.github.maliciousfiles.bloodOnTheClocktower.play;

import com.mojang.datafixers.util.Pair;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.List;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class MiscGameHandler implements Listener {
    private static final ItemStack MYSTERY = CraftItemStack.asNMSCopy(createItem(Material.PAPER, DataComponentPair.model("mystery")));

    @EventHandler
    public void onSelectItem(PlayerItemHeldEvent evt) {
        if (!BloodOnTheClocktower.key("role")
                .equals(evt.getPlayer().getInventory().getItem(evt.getNewSlot()).getData(DataComponentTypes.ITEM_MODEL))) return;

        Bukkit.getOnlinePlayers().forEach(player -> ((CraftPlayer) player).getHandle().connection
                .send(new ClientboundSetEquipmentPacket(player.getEntityId(),
                        List.of(Pair.of(EquipmentSlot.MAINHAND, MYSTERY)))));
    }
}
