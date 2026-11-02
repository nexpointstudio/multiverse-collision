
package com.nexpoint.multiversecollision.item;

import com.nexpoint.multiversecollision.MultiverseCollision;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(
                    ForgeRegistries.ITEMS,
                    MultiverseCollision.MOD_ID
            );

    public static final RegistryObject<Item> SIX_EYES =
            ITEMS.register("six_eyes", () ->
                    new SixEyesItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .rarity(Rarity.RARE)
                    )
            );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
