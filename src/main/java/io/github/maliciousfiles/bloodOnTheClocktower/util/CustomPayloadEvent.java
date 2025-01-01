package io.github.maliciousfiles.bloodOnTheClocktower.util;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomPayloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Class<?> source;
    private final Object data;

    public CustomPayloadEvent(Object data) {
        this.source = StackWalker.getInstance().getCallerClass();
        this.data = data;
    }

    public Class<?> source() {
        return source;
    }
    public Object data() {
        return data;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
