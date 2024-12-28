package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ScriptInfo implements ConfigurationSerializable {

    public final String name, author;
    public final List<RoleInfo> roles;

    public ScriptInfo(String name, String author, List<RoleInfo> roles) {
        this.name = name;
        this.author = author;
        this.roles = roles;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of("name", name, "author", author, "roles", roles.stream().map(RoleInfo::name).toList());
    }

    public static ScriptInfo deserialize(Map<String, Object> args) {
        return new ScriptInfo((String) args.get("name"), (String) args.get("author"), ((List<String>) args.get("roles")).stream().map(RoleInfo::valueOf).toList());
    }
}
