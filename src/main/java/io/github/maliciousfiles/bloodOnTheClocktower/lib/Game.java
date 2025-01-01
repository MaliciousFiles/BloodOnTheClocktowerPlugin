package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.play.SeatList;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.StorytellerPauseHook;
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
        players.forEach(p->p.setGame(this));

        this.uuid = UUID.randomUUID();
        games.put(uuid, this);

        this.seats = seats;
        this.script = script;
        this.storyteller = storyteller;
        this.players = players;
        this.rolesInPlay = this.players.stream().map(BOTCPlayer::getRole).toList();
        this.turn = 1;

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

    public SeatList getSeats() {
        return seats;
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
    public ScriptInfo getScript() {
        return script;
    }

    public void startGame() throws ExecutionException, InterruptedException {
        runNight();
    }

    private void runNight() throws ExecutionException, InterruptedException {
        players.sort((a, b) -> Float.compare(a.getRole().info.nightOrder(), b.getRole().info.nightOrder()));

        for (BOTCPlayer player : players) {
            if (player.getRole().hasAbility()) player.getRole().handleDusk();
        }

        boolean doEvilInfo = turn == 1 && players.size() > 5;
        boolean didMinionInfo = false;
        boolean didDemonInfo = false;
        for (BOTCPlayer player : players) {
            if (doEvilInfo) {
                if (!didMinionInfo && player.getRole().info.nightOrder() > MINION_INFO_ORDER) {
                    didMinionInfo = true;
                    giveMinionInfo();
                }
                if (!didDemonInfo && player.getRole().info.nightOrder() > DEMON_INFO_ORDER) {
                    didDemonInfo = true;
                    giveDemonInfo();
                }
            }

            if (player.getRole().doesSomething(this)) {
                new StorytellerPauseHook(storyteller, "Continue to "+player.getRole().info.name()).get();
                player.getRole().handleNight();
            }
            // TODO: check for game end
        }

        new StorytellerPauseHook(storyteller, "Continue to Dawn").get();
        for (BOTCPlayer player : players) {
            if (player.getRole().hasAbility()) player.getRole().handleDawn();
        }

        new StorytellerPauseHook(storyteller, "Continue to Night").get();
        runNight();
    }

    private void giveMinionInfo() {
        // TODO
    }

    private void giveDemonInfo() {
        // TODO
    }

    public void checkVictory() {
        // TODO
    }
}
