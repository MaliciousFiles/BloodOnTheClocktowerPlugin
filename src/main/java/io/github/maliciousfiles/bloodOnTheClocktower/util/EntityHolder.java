package io.github.maliciousfiles.bloodOnTheClocktower.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class EntityHolder<E extends Entity> {
    private final UUID uuid;

    public EntityHolder(E entity) {
        this.uuid = entity.getUniqueId();
    }

    public boolean exists() {
        return get() != null;
    }

    public E get() {
        return (E) Bukkit.getEntity(uuid);
    }
}
