package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StorytellerPauseHook extends MinecraftHook<Void> {
    private static final Map<UUID, Integer> activeIds = new HashMap<>();

    public StorytellerPauseHook(Storyteller storyteller, String continueText) {
        this(storyteller, continueText, null);
    }

    public StorytellerPauseHook(Storyteller storyteller, String continueText, String cancelText) {
        UUID uuid = storyteller.getPlayer().getUniqueId();

        Component continueComponent = Component.text("Continue", PlayerWrapper.INSTRUCTION_COLOR, TextDecoration.BOLD)
                    .append(Component.text(" - "+continueText).decoration(TextDecoration.BOLD, false));
        storyteller.getPlayer().sendActionBar(continueComponent.append(
                cancelText == null ? Component.empty() : Component.text(" [cancellable]", NamedTextColor.RED)
                        .decoration(TextDecoration.BOLD, false)));

        final int id = activeIds.getOrDefault(uuid, 0)+1;
        activeIds.put(uuid, id);

        Runnable cleanup = () -> {
            if (activeIds.get(uuid) != id) return;

            if (activeIds.get(uuid) == 1) {
                activeIds.remove(uuid);
            } else {
                activeIds.put(uuid, activeIds.get(uuid)-1);
            }

            storyteller.CONTINUE.disable();
            storyteller.CANCEL.disable();
        };

        storyteller.CONTINUE.enable(() -> {
            cleanup.run();
            complete(null);
        }, continueComponent);

        if (cancelText != null) {
            storyteller.CANCEL.enable(() -> {
                cleanup.run();
                cancel();
            }, Component.text("Cancel").decorate(TextDecoration.BOLD)
                    .append(Component.text(" - "+cancelText).decoration(TextDecoration.BOLD, false)));
        }
    }
}
