package io.github.maliciousfiles.bloodOnTheClocktower.play.hooks;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.play.MinecraftHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.concurrent.ExecutionException;

public class AnvilDropHook extends MinecraftHook<Void> {
    private final FallingBlock block;

    public AnvilDropHook(Location loc) throws ExecutionException, InterruptedException {
        block = Bukkit.getScheduler().callSyncMethod(BloodOnTheClocktower.instance,
                () -> loc.getWorld().spawn(loc, FallingBlock.class)).get();

        block.setCancelDrop(true);
        block.setBlockData(Material.ANVIL.createBlockData());
        block.setHurtEntities(false);
    }

    @EventHandler
    public void onAnvil(EntityRemoveFromWorldEvent evt) {
        if (evt.getEntity().equals(block)) complete(null);
    }
}
