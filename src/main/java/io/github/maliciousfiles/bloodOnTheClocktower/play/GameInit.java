package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.StorytellerPauseHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.StorytellerQuestionHook;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Option;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GameInit {
    public static void initGame(List<Location> seats, Location block, Player storytellerPlayer, List<Player> players) throws ExecutionException, InterruptedException {
        Storyteller storyteller = new Storyteller(storytellerPlayer);
        List<BOTCPlayer> botcPlayers = players.stream().map(BOTCPlayer::new).toList();

        SeatList seatList = Bukkit.getScheduler().callSyncMethod(BloodOnTheClocktower.instance, () -> SeatList.initSeats(seats)).get();

        Map<Player, CompletableFuture<Void>> seatInstructions = botcPlayers.stream().collect(Collectors.toMap(PlayerWrapper::getPlayer,
                p -> p.giveInstruction("Pick a seat for the game")));
        CompletableFuture<Void> seatFuture = seatList.selectSeats(players, player -> {
            seatInstructions.get(player).complete(null);
        });

        CompletableFuture<ScriptInfo> scriptFuture = new CompletableFuture<>();
        CompletableFuture<List<RoleInfo>> roleFuture = new CompletableFuture<>();
        ScriptDisplay.open(storytellerPlayer, players.size(), scriptFuture, roleFuture);

        ScriptInfo script = scriptFuture.get();
        List<RoleInfo> roles = roleFuture.get();

        CompletableFuture<Void> waitInstruction = storyteller.giveInstruction("Wait for the players to pick their seats");
        seatFuture.get();
        waitInstruction.complete(null);

        Map<Player, CompletableFuture<Void>> roleBagInstructions = botcPlayers.stream().collect(Collectors.toMap(PlayerWrapper::getPlayer,
                p -> p.giveInstruction("Wait for the the Role Grab Bag")));

        CompletableFuture<Map<Player, RoleInfo>> selectionsFuture = new CompletableFuture<>();
        ItemStack grabBag = GrabBag.createGrabBag(
                roles.stream()
                        .filter(r->r.type() != Role.Type.TRAVELLER && r.type() != Role.Type.FABLED)
                        .map(r->new Option<>(r, r.getItem())).toList(),
                players,
                pair -> {
                    roleBagInstructions.get(pair.getFirst()).complete(null);

                    BOTCPlayer player = botcPlayers.get(players.indexOf(pair.getFirst()));
                    player.setRole(pair.getSecond());
                    player.setAlignment(pair.getSecond().alignment());

                    pair.getFirst().sendMessage(Component.text("You are the ").append(ChatComponents.roleInfo(pair.getSecond())));
                    storyteller.giveInfo(Component.text(pair.getFirst().getName()+" is the ").append(ChatComponents.roleInfo(pair.getSecond())));
                },
                selectionsFuture,
                DataComponentPair.name(Component.text("Role Grab Bag", NamedTextColor.YELLOW, TextDecoration.BOLD)));
        storytellerPlayer.getInventory().addItem(grabBag);

        CompletableFuture<Void> storytellerInstruction = storyteller.giveInstruction("Pass around the grab bag and allow each player to take a role");
        selectionsFuture.get();
        storytellerInstruction.complete(null);
        GrabBag.removeGrabBag(grabBag);

        Game game = new Game(
                seatList,
                Bukkit.getScheduler().callSyncMethod(BloodOnTheClocktower.instance, () -> new ChoppingBlock(block)).get(),
                script,
                storyteller,
                botcPlayers);
        storytellerPlayer.getInventory().addItem(Grimoire.createGrimoire(game));

        game.startGame();
    }
}
