package io.github.maliciousfiles.bloodOnTheClocktower.lib.roles;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.*;

public class Soldier extends Role {

    public Soldier(BOTCPlayer me, Game game, RoleInfo info) {
        super(me, game, info);
    }

    @Override
    public boolean isSafeFromDemon() {
        return !me.isImpaired();
    }
}
