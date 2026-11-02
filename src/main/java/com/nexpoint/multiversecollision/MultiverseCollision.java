
package com.nexpoint.multiversecollision;

import com.nexpoint.multiversecollision.item.ModItems;
import com.nexpoint.multiversecollision.network.ModNetwork;
import com.nexpoint.multiversecollision.power.GojoConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MultiverseCollision.MOD_ID)
public class MultiverseCollision {

    public static final String MOD_ID = "multiversecollision";

    public MultiverseCollision() {

        IEventBus modBus =
                FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modBus);

        ModNetwork.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, GojoConfig.SPEC);

        modBus.addListener(this::addCreativeTab);
    }

    private void addCreativeTab(
            BuildCreativeModeTabContentsEvent event) {

        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.SIX_EYES);
        }
    }
}
