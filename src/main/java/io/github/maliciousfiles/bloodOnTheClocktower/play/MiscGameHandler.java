package io.github.maliciousfiles.bloodOnTheClocktower.play;

import com.mojang.datafixers.util.Pair;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.List;
import java.util.Optional;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class MiscGameHandler implements Listener {
    private static final ItemStack MYSTERY = CraftItemStack.asNMSCopy(createItem(Material.PAPER, DataComponentPair.model("mystery")));

    public static void register() {
        Bukkit.getPluginManager().registerEvents(new MiscGameHandler(), BloodOnTheClocktower.instance);

        PacketManager.registerListener(ClientboundSetEquipmentPacket.class, (_, packet) -> {
            for (int i = 0; i < packet.getSlots().size(); i++) {
                if (BloodOnTheClocktower.key("role").equals(
                        Optional.ofNullable(packet.getSlots().get(i).getSecond().get(DataComponents.ITEM_MODEL))
                                .map(CraftNamespacedKey::fromMinecraft).orElse(null))) {
                    packet.getSlots().set(i, Pair.of(packet.getSlots().get(i).getFirst(), MYSTERY));
                }
            }
        });
    }

    @EventHandler
    public void onToss(PlayerDropItemEvent evt) {
        Key model = evt.getItemDrop().getItemStack().getData(DataComponentTypes.ITEM_MODEL);
        if (model == null || !model.namespace().equals(BloodOnTheClocktower.key("b").namespace())) return;

        switch(model.value()) {
            case "role":
            case "continue":
                evt.setCancelled(true);
                break;
            default:
                break;
        }
    }
}
