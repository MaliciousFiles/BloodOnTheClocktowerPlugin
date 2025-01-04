package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;

import java.util.concurrent.CompletableFuture;

public class StorytellerQuestionHook extends MinecraftHook<Boolean> {
    private final Storyteller storyteller;

    public StorytellerQuestionHook(Storyteller storyteller, String instruction) {
        CompletableFuture<Void> future = (this.storyteller = storyteller).askQuestion(instruction);

        storyteller.YES.enable(() -> {
            future.complete(null);
            complete(true);
        });
        storyteller.NO.enable(() -> {
            future.complete(null);
            complete(false);
        });
    }

    @Override
    protected void cleanup() {
        storyteller.YES.disable();
        storyteller.NO.disable();
    }
}
