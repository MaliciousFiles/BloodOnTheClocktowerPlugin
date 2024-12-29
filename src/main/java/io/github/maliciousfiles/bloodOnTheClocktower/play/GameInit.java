package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Option;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GameInit {
    public static void initGame(List<Location> seats, Player storytellerPlayer, List<Player> players) throws ExecutionException, InterruptedException {
        Storyteller storyteller = new Storyteller(storytellerPlayer);
        List<BOTCPlayer> botcPlayers = players.stream().map(BOTCPlayer::new).toList();

        CompletableFuture<ScriptInfo> scriptFuture = new CompletableFuture<>();
        CompletableFuture<List<RoleInfo>> roleFuture = new CompletableFuture<>();
        ScriptDisplay.open(storytellerPlayer, players.size(), scriptFuture, roleFuture);

        ScriptInfo script = scriptFuture.get();
        List<RoleInfo> roles = roleFuture.get();

        Map<Player, CompletableFuture<Void>> playerInstructions = botcPlayers.stream().collect(Collectors.toMap(PlayerWrapper::getPlayer,
                p -> p.giveInstruction(Component.text("Wait for the the Role Grab Bag, then right click in your inventory to take your role"))));

        CompletableFuture<Map<Player, RoleInfo>> selectionsFuture = new CompletableFuture<>();
        storytellerPlayer.getInventory().addItem(GrabBag.createGrabBag(
                meta ->
                        meta.displayName(Component.text("Role Grab Bag").decoration(TextDecoration.ITALIC, false)),
                roles.stream()
                        .filter(r->r.type() != Role.Type.TRAVELLER && r.type() != Role.Type.FABLED)
                        .map(r->new Option<>(r, r.getItem())).toList(),
                players,
                pair -> {
                    playerInstructions.get(pair.getFirst()).complete(null);
                    botcPlayers.get(players.indexOf(pair.getFirst())).setRole(pair.getSecond());

                    pair.getFirst().sendMessage(Component.text("You are the ").append(ChatComponents.roleInfo(pair.getSecond())));
                    storyteller.giveInfo(Component.text(pair.getFirst().getName()+" is the ").append(ChatComponents.roleInfo(pair.getSecond())));
                },
                selectionsFuture));

        CompletableFuture<Void> storytellerInstruction = storyteller.giveInstruction(Component.text(
                "Pass around the grab bag and allow each player to take a role"
        ));
        selectionsFuture.get();
        storytellerInstruction.complete(null);

        Game game = new Game(
                seats,
                script,
                storyteller,
                botcPlayers);
    }
}
