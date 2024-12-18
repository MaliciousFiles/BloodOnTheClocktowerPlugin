package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Role;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.GetChoiceHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Washerwoman implements Role {
    @Override
    public String getRoleName() {
        return "Washerwoman";
    }

    @Override
    public String getRoleDescription() {
        return "You start knowing that one of two players is a particular Townsfolk";
    }

    @Override
    public Material getIcon() {
        return Material.LOOM;
    }

    @Override
    public float getNightOrder() {
        return 82;
    }

    @Override
    public boolean shouldWake(Game game) {
        return game.getTurn() == 0;
    }

    @Override
    public void doNightAction(BOTCPlayer me, Game game) throws ExecutionException, InterruptedException {
        game.getStoryteller().giveInstruction("Select two players for the Washerwoman");

        CompletableFuture<List<BOTCPlayer>> selectPlayer = new CompletableFuture<>();
        new SelectPlayerHook(game.getStoryteller(), game, 2, p->!p.equals(me), selectPlayer);

        CompletableFuture<Role> getChoice = new CompletableFuture<>();
        new GetChoiceHook<>(game.getRoles().stream().map(r->
                new GetChoiceHook.Option(r, Component.text(r.getRoleName()), r.getIcon())).toList(), getChoice);

        List<BOTCPlayer> players = selectPlayer.get();
        Role role = getChoice.get();

        me.giveInfo("One of " + players.get(0).getName() + " and " + players.get(1).getName() + " is a " + role.getRoleName());
    }
}
