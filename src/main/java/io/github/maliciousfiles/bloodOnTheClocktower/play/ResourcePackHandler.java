package io.github.maliciousfiles.bloodOnTheClocktower.play;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HexFormat;
import java.util.UUID;

public class ResourcePackHandler implements Listener {

    private static final UUID uuid = UUID.fromString("25abeb60-a254-43ab-bb92-d5ec2268ecae"); // randomly generated
    private static final byte[] hash = HexFormat.of().parseHex("0b3db61f0fb650e5cb2e1b72588c1caa7e2781aa"); // SHA1 sum of zip file
    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        evt.getPlayer().addResourcePack(uuid,
                "https://github.com/MaliciousFiles/BloodOnTheClocktowerPlugin/raw/main/BOTC%20Resource%20Pack.zip",
                hash,
                "Resource pack to render custom BOTC items",
                true);
    }
}
