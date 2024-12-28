package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.Role;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.RoleInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GameInit {
    public static void initGame(List<Location> seats, Player storyteller, List<Player> players) throws ExecutionException, InterruptedException {
        CompletableFuture<List<RoleInfo>> wait = new CompletableFuture<>();
        ScriptDisplay.open(storyteller, players.size(), wait);

        List<RoleInfo> roles = wait.get();

        storyteller.getInventory().addItem(GrabBag.createGrabBag(meta -> {
            meta.displayName(Component.text("Role Grab Bag")
                    .decoration(TextDecoration.ITALIC, false));
        }, false, false, roles.stream()
                .filter(r->r.type() != Role.Type.TRAVELLER && r.type() != Role.Type.FABLED)
                .map(RoleInfo::getItem).toList()));
    }
}
