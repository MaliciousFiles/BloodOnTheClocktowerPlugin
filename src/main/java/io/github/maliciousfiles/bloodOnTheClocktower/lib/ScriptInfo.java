package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ScriptInfo implements ConfigurationSerializable {

    public final String name, author;
    public final List<String> roleIds;
    public final List<? extends Class<? extends Role>> roles;

    public ScriptInfo(String name, String author, List<String> roleIds) {
        this.name = name;
        this.author = author;
        this.roleIds = roleIds;
        this.roles = roleIds.stream().map(Role.BY_ID::get).toList();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of("name", name, "author", author, "roles", roleIds);
    }

    public static ScriptInfo deserialize(Map<String, Object> args) {
        return new ScriptInfo((String) args.get("name"), (String) args.get("author"), (List<String>) args.get("roles"));
    }
}