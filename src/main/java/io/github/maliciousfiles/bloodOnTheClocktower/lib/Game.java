package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Game {
    private Map<UUID, BOTCPlayer> mcPlayerToBOTC = new HashMap<>();

    private List<Role> rolesInPlay;
    private Storyteller storyteller;
    private List<BOTCPlayer> players;
    private int turn;

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
        players.sort((a, b) -> Float.compare(a.getRole().getNightOrder(), b.getRole().getNightOrder()));

        players.forEach(BOTCPlayer::onDusk);

        boolean didMinionInfo = false;
        boolean didDemonInfo = false;
        for (BOTCPlayer player : players) {
            if (isFirstNight) {
                if (!didMinionInfo && player.getRole().getNightOrder() > MINION_INFO_ORDER) {
                    didMinionInfo = true;
                    giveMinionInfo();
                }
                if (!didDemonInfo && player.getRole().getNightOrder() > DEMON_INFO_ORDER) {
                    didDemonInfo = true;
                    giveDemonInfo();
                }
            }

            player.getRole().handleNight();
        }

        players.forEach(BOTCPlayer::onDawn);
    }

    private void giveMinionInfo() {
        // TODO
    }

    private void giveDemonInfo() {
        // TODO
    }

    // TODO: put game execution in a thread, at some point in the hierarchy
}
