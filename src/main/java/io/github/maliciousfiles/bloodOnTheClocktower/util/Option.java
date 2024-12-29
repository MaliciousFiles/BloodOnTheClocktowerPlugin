package io.github.maliciousfiles.bloodOnTheClocktower.util;

import org.bukkit.inventory.ItemStack;

public record Option<D>(D data, ItemStack representation) { }
