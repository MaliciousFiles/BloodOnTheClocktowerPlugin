package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.Role;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.RoleInfo;
import net.kyori.adventure.text.Component;

public class ChatComponents {
    public static Component roleInfo(RoleInfo role) {
        return (role.type().equals(Role.Type.TRAVELLER)
                ? Component.text("["+role.title().substring(0, role.title().length()/2), RoleInfo.ROLE_COLORS.get(Role.Type.TOWNSFOLK))
                    .append(Component.text(role.title().substring(role.title().length()/2)+"]", RoleInfo.ROLE_COLORS.get(Role.Type.MINION)))
                : Component.text("["+role.title()+"]", RoleInfo.ROLE_COLORS.get(role.type()))
                .hoverEvent(role.getItem())
        );
    }
}
