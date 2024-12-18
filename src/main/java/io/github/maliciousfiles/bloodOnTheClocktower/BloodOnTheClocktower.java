package io.github.maliciousfiles.bloodOnTheClocktower;

import org.bukkit.plugin.java.JavaPlugin;

public final class BloodOnTheClocktower extends JavaPlugin {
    public static BloodOnTheClocktower instance;

    @Override
    public void onEnable() {
        instance = this;
    }
}
