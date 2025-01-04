package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerAction;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class SelectPlayerHook extends MinecraftHook<List<BOTCPlayer>> {

    private final Predicate<BOTCPlayer> validate;
    private final List<BOTCPlayer> players;
    private final int number;
    private final Game game;
    private final UUID interacter;
    private final boolean shouldGlow;

    private final ItemStack selectSelf;

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

        selectSelf = createItem(Material.PLAYER_HEAD,
                DataComponentPair.of(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(interacter.getPlayer().getPlayerProfile())),
                DataComponentPair.name(Component.text("Select Self", TextColor.color(184, 138, 44))));
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
    public void onInteract(PlayerInteractEvent evt) {
        if (!evt.getPlayer().getUniqueId().equals(interacter) ||
            !selectSelf.equals(evt.getItem())) return;

        select(evt.getPlayer());
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
