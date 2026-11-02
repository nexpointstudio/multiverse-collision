
package com.nexpoint.multiversecollision.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nexpoint.multiversecollision.MultiverseCollision;
import com.nexpoint.multiversecollision.network.ModNetwork;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid = MultiverseCollision.MOD_ID,
        value = Dist.CLIENT
)
public class GojoKeys {

    private static final String CATEGORY =
            "key.categories.multiversecollision";

    private static KeyMapping key(String name, int code) {
        return new KeyMapping(
                "key.multiversecollision." + name,
                InputConstants.Type.KEYSYM,
                code,
                CATEGORY
        );
    }

    public static final KeyMapping BLUE =
            key("blue", GLFW.GLFW_KEY_Z);

    public static final KeyMapping RED =
            key("red", GLFW.GLFW_KEY_X);

    public static final KeyMapping REVERSE =
            key("reverse", GLFW.GLFW_KEY_C);

    public static final KeyMapping PURPLE =
            key("purple", GLFW.GLFW_KEY_V);

    public static final KeyMapping DOMAIN =
            key("domain", GLFW.GLFW_KEY_B);

    public static final KeyMapping INFINITY =
            key("infinity", GLFW.GLFW_KEY_G);

    private static boolean blueHeld = false;

    @Mod.EventBusSubscriber(
            modid = MultiverseCollision.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class Registration {

        @SubscribeEvent
        public static void register(RegisterKeyMappingsEvent event) {
            event.register(BLUE);
            event.register(RED);
            event.register(REVERSE);
            event.register(PURPLE);
            event.register(DOMAIN);
            event.register(INFINITY);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null ||
                minecraft.screen != null) {

            if (blueHeld && minecraft.player != null) {
                ModNetwork.sendAction(1);
            }

            blueHeld = false;
            return;
        }

        // Azul: iniciar e encerrar ao soltar Z
        boolean holding = BLUE.isDown();

        if (holding && !blueHeld) {
            ModNetwork.sendAction(0);
        }

        if (!holding && blueHeld) {
            ModNetwork.sendAction(1);
        }

        blueHeld = holding;

        while (RED.consumeClick()) {
            ModNetwork.sendAction(2);
        }

        while (REVERSE.consumeClick()) {
            ModNetwork.sendAction(3);
        }

        while (PURPLE.consumeClick()) {
            if (Screen.hasShiftDown()) {
                ModNetwork.sendAction(5);
            } else {
                ModNetwork.sendAction(4);
            }
        }

        while (DOMAIN.consumeClick()) {
            ModNetwork.sendAction(6);
        }

        while (INFINITY.consumeClick()) {
            ModNetwork.sendAction(7);
        }
    }
}
