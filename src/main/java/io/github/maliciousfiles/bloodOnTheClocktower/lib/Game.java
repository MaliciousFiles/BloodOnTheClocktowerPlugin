package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.play.SeatList;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Game {
    private static final Map<UUID, Game> games = new HashMap<>();
    public static Game getGame(UUID id) {
        return games.get(id);
    }

    private final Map<UUID, BOTCPlayer> mcPlayerToBOTC = new HashMap<>();
    private final UUID uuid;
    private final ScriptInfo script;
    private final SeatList seats;
    private final List<Role> rolesInPlay;
    private final Storyteller storyteller;
    private final List<BOTCPlayer> players;
    private int turn;

    public Game(SeatList seats, ScriptInfo script, Storyteller storyteller, List<BOTCPlayer> players) {
        this.uuid = UUID.randomUUID();
        games.put(uuid, this);

        this.seats = seats;
        this.script = script;
        this.storyteller = storyteller;
        this.players = players;
        this.rolesInPlay = this.players.stream().map(BOTCPlayer::getRole).toList();
        this.turn = 0;

        players.forEach(p -> mcPlayerToBOTC.put(p.getPlayer().getUniqueId(), p));
    }
    public UUID getId() {
        return uuid;
    }

    public static final int MINION_INFO_ORDER = 12;
    private static final int DEMON_INFO_ORDER = 15;

    public BOTCPlayer getBOTCPlayer(Player mcPlayer) {
        return mcPlayerToBOTC.get(mcPlayer.getUniqueId());
    }

    public int getTurn() {
        return turn;
    }
    public Storyteller getStoryteller() {
        return storyteller;
    }
    public List<Role> getRoles() {
        return rolesInPlay;
    }

    private void runNight() throws ExecutionException, InterruptedException {
        boolean isFirstNight = turn == 0;
        players.sort((a, b) -> Float.compare(a.getRole().info.nightOrder(), b.getRole().info.nightOrder()));

//        players.forEach(BOTCPlayer::onDusk);

        boolean didMinionInfo = false;
        boolean didDemonInfo = false;
        for (BOTCPlayer player : players) {
            if (isFirstNight) {
                if (!didMinionInfo && player.getRole().info.nightOrder() > MINION_INFO_ORDER) {
                    didMinionInfo = true;
                    giveMinionInfo();
                }
                if (!didDemonInfo && player.getRole().info.nightOrder() > DEMON_INFO_ORDER) {
                    didDemonInfo = true;
                    giveDemonInfo();
                }
            }

            player.getRole().handleNight();
        }

//        players.forEach(BOTCPlayer::onDawn);
    }

    private void giveMinionInfo() {
        // TODO
    }

    private void giveDemonInfo() {
        // TODO
    }

    // TODO: put game execution in a thread, at some point in the hierarchy
}
