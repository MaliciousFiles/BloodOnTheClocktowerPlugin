package io.github.maliciousfiles.bloodOnTheClocktower.util;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.PaperItemLore;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class DataComponentPair<D> extends Pair<DataComponentType.Valued<D>, D> {
    protected DataComponentPair(DataComponentType.Valued<D> first, D second) {
        super(first, second);
    }

    public ItemStack apply(ItemStack item) {
        item.setData(getFirst(), getSecond());
        return item;
    }

    public static DataComponentPair<Component> name(Component name) {
        return of(DataComponentTypes.ITEM_NAME, name);
    }
    public static DataComponentPair<ItemLore> lore(Component... loreArr) {
        return of(DataComponentTypes.LORE, ItemLore.lore(Stream.of(loreArr).map(c->{
            if (c.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
                return c.decoration(TextDecoration.ITALIC, false);
            }
            return c;
        }).toList()));
    }
    public static DataComponentPair<CustomModelData> cmd(Object... values) {
        CustomModelData.Builder cmd = CustomModelData.customModelData();

        for (Object value : values) {
            switch (value) {
                case Float f -> cmd.addFloat(f);
                case Boolean b -> cmd.addFlag(b);
                case String s -> cmd.addString(s);
                case Color c -> cmd.addColor(c);
                default -> throw new IllegalArgumentException("Invalid CustomModelData value type: " + value.getClass());
            }
        }

        return of(DataComponentTypes.CUSTOM_MODEL_DATA, cmd.build());
    }
    public static DataComponentPair<Key> model(String name) {
        return of(DataComponentTypes.ITEM_MODEL, BloodOnTheClocktower.key(name));
    }
    public static DataComponentPair<Void> custom(Pair<NamespacedKey, Tag>... tags) {
        CompoundTag tag = new CompoundTag();
        for (Pair<NamespacedKey, Tag> pair : tags) tag.put(pair.getFirst().toString(), pair.getSecond());

        return new DataComponentPair<>(null, null) {
            @Override
            public ItemStack apply(ItemStack item) {
                net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
                CustomData.set(DataComponents.CUSTOM_DATA, nms, tag);

                return CraftItemStack.asBukkitCopy(nms);
            }
        };
    }
    public static <D> DataComponentPair<D> of(DataComponentType.Valued<D> first, D second) {
        return new DataComponentPair<>(first, second);
    }



    public static <T extends Tag> T getCustomData(ItemStack item, NamespacedKey key) {
        return Optional.ofNullable(CraftItemStack.asNMSCopy(item).get(DataComponents.CUSTOM_DATA)).map(c -> (T) c.copyTag().get(key.toString())).orElse(null);
    }
}
