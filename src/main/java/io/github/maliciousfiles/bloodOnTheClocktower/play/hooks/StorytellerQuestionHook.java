package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.Storyteller;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;

import java.util.concurrent.CompletableFuture;

public class StorytellerQuestionHook extends MinecraftHook<Boolean> {

    public StorytellerQuestionHook(Storyteller storyteller, String instruction) {
        CompletableFuture<Void> future = storyteller.askQuestion(instruction);

        storyteller.YES.enable(() -> {
            storyteller.YES.disable();
            storyteller.NO.disable();
            future.complete(null);
            complete(true);
        });
        storyteller.NO.enable(() -> {
            storyteller.YES.disable();
            storyteller.NO.disable();
            future.complete(null);
            complete(false);
        });
    }
}
