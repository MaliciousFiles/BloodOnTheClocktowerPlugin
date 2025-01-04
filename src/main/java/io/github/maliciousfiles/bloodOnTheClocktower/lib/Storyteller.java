package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

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

    public Storyteller(Player mcPlayer) {
        super(mcPlayer);
    }

    @Override
    public void setupInventory() {
        super.setupInventory();
        CONTINUE.disable();
        CANCEL.disable();
        YES.disable();
        NO.disable();
        NOMINATE.disable();
    }

    public void enableCancel(Runnable action, String text) {
        ;
    }
}
