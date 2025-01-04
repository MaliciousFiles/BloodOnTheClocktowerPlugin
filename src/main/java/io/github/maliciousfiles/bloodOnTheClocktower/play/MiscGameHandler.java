package io.github.maliciousfiles.bloodOnTheClocktower.play;

import com.mojang.datafixers.util.Pair;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.RoleInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class MiscGameHandler implements Listener {
    private static final ItemStack MYSTERY = CraftItemStack.asNMSCopy(createItem(Material.PAPER, DataComponentPair.model("mystery")));

    public static void register() {
        Bukkit.getPluginManager().registerEvents(new MiscGameHandler(), BloodOnTheClocktower.instance);

        PacketManager.registerListener(ClientboundSetEquipmentPacket.class, (_, packet) -> {
            for (int i = 0; i < packet.getSlots().size(); i++) {
                ResourceLocation key = packet.getSlots().get(i).getSecond().get(DataComponents.ITEM_MODEL);
                if (key == null || !key.getNamespace().equals(BloodOnTheClocktower.key("b").namespace())) continue;

                switch(key.getPath()) {
                    case "role":
                    case "no":
                    case "yes":
                        packet.getSlots().set(i, Pair.of(packet.getSlots().get(i).getFirst(), MYSTERY));
                    default:
                        break;
                }
            }
        });
    }

    @EventHandler
    public void onToss(PlayerDropItemEvent evt) {
        if (RoleInfo.isRoleItem(evt.getItemDrop().getItemStack())) {
            evt.setCancelled(true);
        } else if (GrabBag.isGrabBag(evt.getItemDrop().getItemStack())) {
            evt.getItemDrop().setPickupDelay(10);
        }
    }
}
