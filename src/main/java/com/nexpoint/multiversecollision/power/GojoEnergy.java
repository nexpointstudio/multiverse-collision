
package com.nexpoint.multiversecollision.power;

import com.nexpoint.multiversecollision.item.ModItems;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

public class GojoEnergy {

    public static final float MAX_ENERGY = 100.0f;
    public static final float REGEN_PER_SECOND = 2.0f;

    public static final String DATA_KEY =
            "multiversecollision_gojo";

    public static boolean hasSixEyes(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD)
                .is(ModItems.SIX_EYES.get());
    }

    public static CompoundTag getData(Player player) {
        CompoundTag root = player.getPersistentData();

        if (!root.contains(DATA_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag data = new CompoundTag();

            data.putFloat("energy", MAX_ENERGY);
            data.putBoolean("infinity", false);
            data.putLong("reverse_cooldown", 0L);

            root.put(DATA_KEY, data);
        }

        return root.getCompound(DATA_KEY);
    }

    public static float getEnergy(Player player) {
        return Math.max(0, Math.min(MAX_ENERGY, getData(player).getFloat("energy")));
    }

    public static void setEnergy(Player player, float amount) {
        float value = Math.max(
                0.0f,
                Math.min(MAX_ENERGY, amount)
        );

        getData(player).putFloat("energy", value);
    }

    public static boolean consume(Player player, float cost) {
        if (!Float.isFinite(cost) || cost < 0 || getEnergy(player) < cost) {
            return false;
        }

        setEnergy(player, getEnergy(player) - cost);
        return true;
    }

    public static void regenerate(Player player) {
        float amount = GojoConfig.REGEN.get().floatValue() / 20.0f;

        setEnergy(player, getEnergy(player) + amount);
    }

    public static boolean isInfinityActive(Player player) {
        return hasSixEyes(player)
                && getData(player).getBoolean("infinity")
                && getEnergy(player) > 0;
    }
}
