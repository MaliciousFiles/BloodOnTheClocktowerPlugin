package io.github.maliciousfiles.bloodOnTheClocktower.play;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.BOTCPlayer;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.util.Mth;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public class ChoppingBlock {
    private static final int TICKS_PER_ROT = 40;

    private final TextDisplay text;
    private final ItemDisplay head;

    private BOTCPlayer onTheBlock;
    private int votes;

    public ChoppingBlock(Location loc) {
        this.head = loc.getWorld().spawn(loc.add(0.5, 1.9, 0.5), ItemDisplay.class);
        this.text = loc.getWorld().spawn(loc.add(0, 0.2, 0), TextDisplay.class);
        text.setSeeThrough(false);
        text.setBillboard(Display.Billboard.CENTER);
        text.setBackgroundColor(Color.fromARGB(0));

        Matrix4f mat = new Matrix4f();
        Bukkit.getScheduler().runTaskTimer(BloodOnTheClocktower.instance, () -> {
            head.setTransformationMatrix(mat.rotateY(Math.toRadians(120)));
            head.setInterpolationDelay(0);
            head.setInterpolationDuration(TICKS_PER_ROT/3);
        }, 0, TICKS_PER_ROT/3);

        entities.add(head);
        entities.add(text);
    }

    public void clear() {
        head.setItemStack(null);
        text.text(Component.empty());

        this.onTheBlock = null;
        this.votes = 0;
    }

    public BOTCPlayer getOnTheBlock() {
        return onTheBlock;
    }
    public int getVotes() {
        return votes;
    }

    public void setVotesNecessary(int votes) {
        text.text(Component.text("Needs ")
                .append(Component.text(votes).decoration(TextDecoration.BOLD, true))
                .append(Component.text(" to execute"))
                .color(NamedTextColor.DARK_GRAY));
    }

    public void setPlayerWithVotes(BOTCPlayer player, int votes) {
        this.onTheBlock = player;
        this.votes = votes;

        head.setItemStack(createItem(Material.PLAYER_HEAD,
                DataComponentPair.of(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(player.getPlayer().getPlayerProfile()))));
        text.text(Component.text(player.getName(), NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
                .append(Component.text("\n(" + votes + " votes)")
                        .decoration(TextDecoration.BOLD, false)));
    }

    private static final List<Entity> entities = new ArrayList<>();
    public static void destruct() {
        entities.forEach(Entity::remove);
    }
}
