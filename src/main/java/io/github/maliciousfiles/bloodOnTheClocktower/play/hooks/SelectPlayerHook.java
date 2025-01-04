package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class SelectPlayerHook extends MinecraftHook<List<BOTCPlayer>> {

    private final Predicate<BOTCPlayer> validate;
    private final List<BOTCPlayer> players;
    private final int number;
    private final Game game;
    private final UUID interacter;
    private final boolean shouldGlow;

    public SelectPlayerHook(PlayerWrapper interacter, Game game, int number, Predicate<BOTCPlayer> validate) {
        this(interacter, game, number, validate, true);
    }

    public SelectPlayerHook(PlayerWrapper interacter, Game game, int number, Predicate<BOTCPlayer> validate, boolean shouldGlow) {
        this.number = number;
        this.validate = validate;
        this.players = new ArrayList<>();
        this.game = game;
        this.interacter = interacter.getPlayer().getUniqueId();

        this.shouldGlow = shouldGlow;
    }

    private void select(Player other) {
        BOTCPlayer interacted = game.getBOTCPlayer(other);
        if (interacted == null || !validate.test(interacted)) return;

        players.add(interacted);

        if (shouldGlow) {
            List<PlayerWrapper> toGlow = new ArrayList<>(game.getAwake());
            toGlow.add(game.getStoryteller());
            interacted.glow(toGlow);
        }

        if (players.size() == number) complete(new ArrayList<>(players));
    }

    @EventHandler
    public void onPunch(PrePlayerAttackEntityEvent evt) {
        if (!evt.getPlayer().getUniqueId().equals(interacter) ||
                !(evt.getAttacked() instanceof Player other)) return;

        select(other);
        evt.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent evt) {
        if (!evt.getPlayer().getUniqueId().equals(interacter) ||
            !(evt.getRightClicked() instanceof Player other)) return;

        select(other);
    }

    @Override
    protected void cleanup() {
        if (isCancelled()) players.forEach(PlayerWrapper::deglow);
    }
}
