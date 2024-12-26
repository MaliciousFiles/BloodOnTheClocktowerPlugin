package io.github.maliciousfiles.bloodOnTheClocktower.util;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class BOTCConfiguration extends YamlConfiguration {
    private final File file;

    public BOTCConfiguration(File file) {
        super();

        this.file = file;
        try { load(file); }
        catch (IOException | InvalidConfigurationException _) {}
    }

    public void save() {
        try {
            save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BOTCConfiguration getConfig(String name) {
        return new BOTCConfiguration(new File(BloodOnTheClocktower.instance.getDataFolder(), name));
    }
}