package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;

public class Soldier extends Role {

    public Soldier(BOTCPlayer me, Game game, RoleInfo info) {
        super(me, game, info);
    }

    @Override
    public void handleDeathAttempt(BOTCPlayer.DeathCause cause, @Nullable BOTCPlayer killer) throws ExecutionException, InterruptedException {
        if (cause == BOTCPlayer.DeathCause.PLAYER && killer.getRoleInfo().type() == Type.DEMON) {
            game.log("resisted dying to {0}", me, Game.LogPriority.MEDIUM, killer);
        } else {
            super.handleDeathAttempt(cause, killer);
        }
    }
}
