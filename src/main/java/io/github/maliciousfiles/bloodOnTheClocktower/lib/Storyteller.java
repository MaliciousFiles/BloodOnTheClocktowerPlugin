package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.play.Grimoire;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;

public class Storyteller extends PlayerWrapper {
    public final PlayerAction CONTINUE = new PlayerAction(getPlayer(),
            "Continue", TextColor.color(102, 255, 144),
            "Right click to continue the game", "continue", 8);
    public final PlayerAction CANCEL = new PlayerAction(getPlayer(),
            "Cancel", TextColor.color(255, 21, 21),
            "Right click to cancel the current task", "no", 7);
    public final PlayerAction YES = new PlayerAction(getPlayer(),
            "Yes", TextColor.color(24, 255, 45),
            "Right click to answer yes", "yes", 6);
    public final PlayerAction NO = new PlayerAction(getPlayer(),
            "No", TextColor.color(255, 21, 21),
            "Right click to answer no", "no", 5);
    public final PlayerAction NOMINATE = new PlayerAction(getPlayer(),
            "Nominate", TextColor.color(51, 186, 255),
            "Right click to nominate a player", "nominate", 4);
    public final PlayerAction EDIT_PLAYER = new PlayerAction(getPlayer(),
            "Edit Player", TextColor.color(224, 161, 76),
            "Look at a player to open their edit menu", NamespacedKey.minecraft("spyglass"), 4) {
        public void rightClick(PlayerInteractEvent evt) {
            Bukkit.getScheduler().runTaskLater(BloodOnTheClocktower.instance, () -> {
                if (isItem(evt.getPlayer().getActiveItem()) && evt.getPlayer().getActiveItemUsedTime() >= 20) {
                    super.rightClick(evt);
                    evt.getPlayer().clearActiveItem();
                }
            }, 20);
        }
    };

    private Game game;
    public Storyteller(Player mcPlayer) {
        super(mcPlayer);
    }
    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void setupInventory() {
        super.setupInventory();
        CONTINUE.disable();
        CANCEL.disable();
        YES.disable();
        NO.disable();
        NOMINATE.disable();
        EDIT_PLAYER.enable(() -> {
            RayTraceResult res = getPlayer().rayTraceEntities(40);
            if (res == null || !(res.getHitEntity() instanceof Player player)) return;

            Grimoire.editPlayer(game, getPlayer(), Grimoire.Access.STORYTELLER, Component.text("Storyteller's Grimoire"), game.getBOTCPlayer(player));
        });
    }

    public void enableCancel(Runnable action, String text) {
        ;
    }
}
