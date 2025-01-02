package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.play.SeatList;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.StorytellerPauseHook;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class Game {
    private static final Map<UUID, Game> games = new HashMap<>();
    public static Game getGame(UUID id) {
        return games.get(id);
    }

    public enum Winner { NONE, GOOD, EVIL }

    private final Map<UUID, BOTCPlayer> mcPlayerToBOTC = new HashMap<>();
    private final UUID uuid;
    private final ScriptInfo script;
    private final SeatList seats;
    private final List<Role> rolesInPlay;
    private final Storyteller storyteller;
    private final List<BOTCPlayer> players;
    private int turn;
    private Winner winner = Winner.NONE;

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
        setup();
        while (true) {
            runNight();
            if (isGameOver()) { break; }
        }
        // TODO: announce game end
    }

    private void setup() throws ExecutionException, InterruptedException {
        for (BOTCPlayer player : players) {
            new StorytellerPauseHook(storyteller, "Setup "+player.getRole().info.name()).get();
            player.getRole().setup();
        }
    }

    public interface NightAction {
        String name();
        boolean shouldRun();
        float order();
        void run() throws ExecutionException, InterruptedException;
    }

    private class MinionInfoNightAction implements NightAction {
        @Override
        public String name() { return "Minion Info"; }

        @Override
        public boolean shouldRun() { return true; }

        @Override
        public float order() { return 12; }

        @Override
        public void run() throws ExecutionException, InterruptedException {
            // TODO
        }
    }

    private class DemonInfoNightAction implements NightAction {
        @Override
        public String name() { return "Demon Info"; }

        @Override
        public boolean shouldRun() { return true; }

        @Override
        public float order() { return 15; }

        @Override
        public void run() throws ExecutionException, InterruptedException {
            // TODO
        }
    }

    private void runNight() throws ExecutionException, InterruptedException {
        if (isGameOver()) { return; }

        PriorityQueue<NightAction> nightActions = new PriorityQueue<>(Comparator.comparing(NightAction::order));

        new StorytellerPauseHook(storyteller, "Continue to Night").get();
        for (BOTCPlayer player : players) {
            if (player.getRole().hasAbility()) player.getRole().handleDusk();
            nightActions.add(player.getRole().getNightAction());
        }

        if (turn == 1 && players.size() > 5) {
            nightActions.add(new MinionInfoNightAction());
            nightActions.add(new DemonInfoNightAction());
        }

        while (!nightActions.isEmpty()) {
            if (isGameOver()) { return; }

            NightAction action = nightActions.poll();
            if (action.shouldRun()) {
                new StorytellerPauseHook(storyteller, "Continue to " + action.name()).get();
                action.run();
            }
        }

        if (isGameOver()) { return; }
        new StorytellerPauseHook(storyteller, "Continue to Dawn").get();
        for (BOTCPlayer player : players) {
            if (player.getRole().hasAbility()) player.getRole().handleDawn();
        }
    }

    public boolean isGameOver() {
        return winner != Winner.NONE;
    }

    public void checkVictory() {
        if (isGameOver()) { return; }

        int alive = 0;
        boolean demonAlive = false;
        boolean goodVictoryBlocked = false;
        for (BOTCPlayer player : players) {
            if (player.getRole().countsAsAlive()) {
                alive++;
                if (player.getRole().info.type() == Role.Type.DEMON) {
                    demonAlive = true;
                    break;
                }
            }
            if (player.getRole().blocksGoodVictory()) {
                goodVictoryBlocked = true;
                break;
            }
        }

        if (!demonAlive && !goodVictoryBlocked) {
            winner = Winner.GOOD;
        } else if (alive <= 2) {
            winner = Winner.EVIL;
        }
    }
}
