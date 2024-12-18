package io.github.maliciousfiles.bloodOnTheClocktower.play;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public abstract class PlayerWrapper {
    private final Player mcPlayer;

    public PlayerWrapper(Player mcPlayer) {
        this.mcPlayer = mcPlayer;
    }

    public Player getPlayer() {
        return mcPlayer;
    }
    public String getName() {
        return mcPlayer.getName();
    }

    public void giveInstruction(String instruction) {
        // TODO: make this stay on screen until dismissed somehow
        mcPlayer.sendActionBar(Component.text(instruction));
    }
    public void giveInfo(String info) {
        mcPlayer.sendMessage(Component.text(info));
    }
}
