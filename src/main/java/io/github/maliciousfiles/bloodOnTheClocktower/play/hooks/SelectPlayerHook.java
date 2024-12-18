package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class SelectPlayerHook extends MinecraftHook<PlayerInteractEntityEvent, List<BOTCPlayer>> {

    private final Predicate<BOTCPlayer> validate;
    private final List<BOTCPlayer> players;
    private final int number;
    private final Game game;
    private final UUID interacter;

    public SelectPlayerHook(PlayerWrapper interacter, Game game, int number, Predicate<BOTCPlayer> validate, CompletableFuture<List<BOTCPlayer>> complete) {
        super(PlayerInteractEntityEvent.class, complete);

        this.number = number;
        this.validate = validate;
        this.players = new ArrayList<>();
        this.game = game;
        this.interacter = interacter.getPlayer().getUniqueId();
    }

    @Override
    protected void onEvent(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().getUniqueId().equals(interacter) ||
            !(event.getRightClicked() instanceof Player other)) return;

        BOTCPlayer interacted = game.getBOTCPlayer(other);
        if (interacted == null || !validate.test(interacted)) return;

        players.add(interacted);
        // TODO: make glowing, give feedback, etc.

        if (players.size() == number) complete(players);
    }
}
