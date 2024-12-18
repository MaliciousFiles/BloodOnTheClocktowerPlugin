package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import org.bukkit.Material;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface Role {
    String getRoleName();
    String getRoleDescription();
    Material getIcon();
    float getNightOrder(); // https://docs.google.com/spreadsheets/d/1eJkBC6rF-VU6J0h0KJvyiXjs2HLl6Yjzw9jfVYHOW34/edit

    boolean shouldWake(Game game);
    void doNightAction(BOTCPlayer me, Game game) throws InterruptedException, ExecutionException;

    enum DeathCause { STORY, EXECUTION, PLAYER }
    default boolean canDieTo(DeathCause cause, @Nullable BOTCPlayer killer, Game game) { return true; }
}
