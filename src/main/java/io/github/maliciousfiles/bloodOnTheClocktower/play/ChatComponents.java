package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.ReminderToken;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Role;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.RoleInfo;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Pair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.nbt.IntTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ChatComponents {
    public static Component roleInfo(RoleInfo role) {
        return (role.type().equals(Role.Type.TRAVELLER)
                ? Component.text("["+role.title().substring(0, role.title().length()/2), RoleInfo.ROLE_COLORS.get(Role.Type.TOWNSFOLK))
                    .append(Component.text(role.title().substring(role.title().length()/2)+"]", RoleInfo.ROLE_COLORS.get(Role.Type.MINION)))
                : Component.text("["+role.title()+"]", RoleInfo.ROLE_COLORS.get(role.type())))
                .hoverEvent(role.getItem());
    }

    public static Component playerInfo(BOTCPlayer player, TextColor color) {
        return Component.text("the ", color).append(roleInfo(player.getRoleInfo()))
                .append(Component.text(" ("+player.getName()+")", NamedTextColor.WHITE));
    }

    public static Component substitutePlayerInfo(String message, TextColor color, BOTCPlayer... players) {
        final String regex = "\\{\\d+}";

        Component result = Component.empty();
        for (String part : message.splitWithDelimiters(regex, -1)) {
            if (part.matches(regex)) {
                int idx = Integer.parseInt(part.substring(1, part.length()-1));
                result = result.append(idx < players.length ? ChatComponents.playerInfo(players[idx], color)
                        : Component.text("<player not given>"));
            } else {
                result = result.append(Component.text(part, color));
            }
        }

        return result;
    }
}
