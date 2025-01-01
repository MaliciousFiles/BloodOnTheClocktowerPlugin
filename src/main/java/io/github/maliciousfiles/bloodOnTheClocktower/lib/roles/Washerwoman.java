package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ChatComponents;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.GetChoiceHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.SelectPlayerHook;
import io.github.maliciousfiles.bloodOnTheClocktower.util.Option;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Washerwoman extends Role {
    boolean hasInfo = false;

    public Washerwoman(BOTCPlayer me, Game game, RoleInfo info) { super(me, game, info); }

    @Override
    public void setup() throws ExecutionException, InterruptedException {
        Storyteller st = game.getStoryteller();
        st.giveInstruction("Assign the Washerwoman's Townsfolk and Wrong reminder tokens");

        CompletableFuture<BOTCPlayer> townsfolk = new CompletableFuture<>();
//        new PlayerChoiceHook(game.getStoryteller(), game, "Assign the Washerwoman's Townsfolk reminder token", null, townsfolk);
        newReminderToken(new ReminderToken("Townsfolk", me, townsfolk.get(), ReminderToken.Effect.NONE));

        CompletableFuture<BOTCPlayer> wrong = new CompletableFuture<>();
//        new PlayerChoiceHook(game.getStoryteller(), game, "Assign the Washerwoman's Wrong reminder token", null, wrong);
        newReminderToken(new ReminderToken("Wrong", me, wrong.get(), ReminderToken.Effect.NONE));
    }

    @Override
    public void handleNight() throws ExecutionException, InterruptedException {
        if (hasInfo) { return; }
        hasInfo = true;

        me.wake();

        game.getStoryteller().giveInstruction("Select two players for the Washerwoman");

        CompletableFuture<List<BOTCPlayer>> selectPlayer = new CompletableFuture<>();
        new SelectPlayerHook(game.getStoryteller(), game, 2, p->!p.equals(me), selectPlayer);

        CompletableFuture<Role> getChoice = new CompletableFuture<>();
        new GetChoiceHook<>(game.getRoles().stream().map(r->
                new Option<>(r, r.info.getItem())).toList(), getChoice);

        List<BOTCPlayer> players = selectPlayer.get();
        Role role = getChoice.get();

        me.giveInfo(Component.text("One of " + players.get(0).getName() + " and " + players.get(1).getName() + " is a " + ChatComponents.roleInfo(role.info)));

        me.sleep();
    }

}
