package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StorytellerPauseHook extends MinecraftHook<Void> {
    private static final Map<UUID, Integer> activeIds = new HashMap<>();

    private final UUID uuid;
    private final int id;
    private final Storyteller storyteller;
    public StorytellerPauseHook(Storyteller storyteller, String continueText) {
        uuid = (this.storyteller = storyteller).getPlayer().getUniqueId();

        Component continueComponent = Component.text("Continue", storyteller.CONTINUE.color(), TextDecoration.BOLD)
                    .append(Component.text(" - "+continueText).decoration(TextDecoration.BOLD, false));
        if (!storyteller.CONTINUE.isItem(storyteller.getPlayer().getInventory().getItemInMainHand())) {
            storyteller.getPlayer().sendActionBar(continueComponent);
        }

        id = activeIds.getOrDefault(uuid, 0)+1;
        activeIds.put(uuid, id);

        storyteller.CONTINUE.enable(() -> complete(null), continueComponent);
    }

    @Override
    protected void cleanup() {
        if (activeIds.get(uuid) != id) return;

        if (activeIds.get(uuid) == 1) {
            activeIds.remove(uuid);
        } else {
            activeIds.put(uuid, activeIds.get(uuid)-1);
        }

        storyteller.CONTINUE.disable();
    }
}
