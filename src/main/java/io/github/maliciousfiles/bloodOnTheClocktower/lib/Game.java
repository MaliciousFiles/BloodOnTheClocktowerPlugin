package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import com.google.common.collect.BiMap;
import org.bukkit.Bukkit;
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
        players.forEach(BOTCPlayer::tickEffects);

        for (BOTCPlayer player : players) {
            // TODO: Storyteller#prompt for player to wake up, with info on who they are
            if (player.getRole().shouldWake(this)) {
                player.setAwake(true);
                player.getRole().doNightAction(player, this);
                player.setAwake(false);
            }
        }
    }

    // TODO: put game execution in a thread, at some point in the hierarchy
}
