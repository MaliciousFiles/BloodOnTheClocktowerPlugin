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
        players.sort((a, b) -> Float.compare(isFirstNight ? a.getRole().getFirstNightOrder() : a.getRole().getNormalNightOrder(),
                                             isFirstNight ? b.getRole().getFirstNightOrder() : b.getRole().getNormalNightOrder()));

        players.forEach(BOTCPlayer::onDusk);

        for (BOTCPlayer player : players) {
            player.getRole().handleNight(player, this);
        }

        players.forEach(BOTCPlayer::onDawn);
    }

    // TODO: put game execution in a thread, at some point in the hierarchy
}
