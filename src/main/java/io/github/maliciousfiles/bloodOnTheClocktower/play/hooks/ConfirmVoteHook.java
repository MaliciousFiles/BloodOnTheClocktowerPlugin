package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.SeatList;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.function.Consumer;

public class ConfirmVoteHook extends MinecraftHook<Boolean> {
    private final BOTCPlayer voter;
    private final Player storyteller;
    private final Consumer<SeatList.VoteState> setVote;

    public ConfirmVoteHook(BOTCPlayer voter, Storyteller storyteller, Consumer<SeatList.VoteState> setVote) {
        (this.voter = voter).VOTE.enable(null);
        this.storyteller = storyteller.getPlayer();
        this.setVote = setVote;
    }

    @EventHandler
    public void onSelect(PlayerItemHeldEvent evt) {
        if (!evt.getPlayer().equals(voter.getPlayer())) return;

        setVote.accept(voter.VOTE.isItem(evt.getPlayer().getInventory().getItem(evt.getNewSlot()))
                ? SeatList.VoteState.MAYBE
                : SeatList.VoteState.NO);
    }

    private void handleSelect() {
        boolean vote = voter.VOTE.isItem(voter.getPlayer().getInventory().getItemInMainHand());

        setVote.accept(vote ? SeatList.VoteState.CONFIRMED : SeatList.VoteState.NO);
        voter.VOTE.disable();

        complete(vote);
    }

    @EventHandler
    public void onPunch(PrePlayerAttackEntityEvent evt) {
        if (!evt.getPlayer().equals(storyteller) || !evt.getAttacked().equals(voter.getPlayer())) return;

        evt.setCancelled(true);
        handleSelect();
    }
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent evt) {
        if (!evt.getPlayer().equals(storyteller) || !evt.getRightClicked().equals(voter.getPlayer())) return;

        evt.setCancelled(true);
        handleSelect();
    }
}
