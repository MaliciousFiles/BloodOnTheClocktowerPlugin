package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StorytellerPauseHook extends MinecraftHook<Void> {
    private static final Map<UUID, Integer> activeIds = new HashMap<>();

    private final int id;
    public StorytellerPauseHook(Storyteller storyteller, String instruction) {
        UUID uuid = storyteller.getPlayer().getUniqueId();
        CompletableFuture<Void> future = storyteller.giveInstruction(instruction);

        id = activeIds.getOrDefault(uuid, 0)+1;
        activeIds.put(uuid, id);

        storyteller.CONTINUE.enable(() -> {
            if (activeIds.get(uuid) != id) return;

            if (activeIds.get(uuid) == 1) activeIds.remove(uuid);
            else activeIds.put(uuid, activeIds.get(uuid)-1);

            storyteller.CONTINUE.disable();
            future.complete(null);
            complete(null);
        });
    }
}
