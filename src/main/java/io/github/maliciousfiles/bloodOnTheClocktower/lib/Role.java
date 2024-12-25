package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import org.bukkit.Material;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface Role {
    String getRoleName();
    String getRoleDescription();
    Material getIcon();
    float getFirstNightOrder(); // https://docs.google.com/spreadsheets/d/1eJkBC6rF-VU6J0h0KJvyiXjs2HLl6Yjzw9jfVYHOW34/edit
    float getNormalNightOrder(); // https://docs.google.com/spreadsheets/d/1eJkBC6rF-VU6J0h0KJvyiXjs2HLl6Yjzw9jfVYHOW34/edit

    default void handleNight(BOTCPlayer me, Game game) throws InterruptedException, ExecutionException {
        if (game.getTurn() == 0 && getFirstNightOrder() >= 0 || game.getTurn() > 0 && getNormalNightOrder() >= 0) {
            me.wake();
            doNightAction(me, game);
            me.sleep();
        }
    }
    void doNightAction(BOTCPlayer me, Game game) throws InterruptedException, ExecutionException;

    enum DeathCause { STORY, EXECUTION, PLAYER }
    default boolean canDieTo(DeathCause cause, @Nullable BOTCPlayer killer, Game game) { return true; }
}
