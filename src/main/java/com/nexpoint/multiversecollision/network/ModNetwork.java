
package com.nexpoint.multiversecollision.network;

import com.nexpoint.multiversecollision.MultiverseCollision;
import com.nexpoint.multiversecollision.item.ModItems;
import com.nexpoint.multiversecollision.power.GojoPowers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ModNetwork {

    private static final String VERSION = "1";

    // Canal de comunicacao entre cliente e servidor
    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(
                            MultiverseCollision.MOD_ID,
                            "main"
                    ),
                    () -> VERSION,
                    VERSION::equals,
                    VERSION::equals
            );

    // Registro dos pacotes de rede
    public static void register() {

        CHANNEL.registerMessage(
                0,
                ActionPacket.class,
                ActionPacket::encode,
                ActionPacket::decode,
                ActionPacket::handle
        );
    }

    // Envia uma acao do teclado para o servidor
    public static void sendAction(int action) {

        CHANNEL.sendToServer(
                new ActionPacket((byte) action)
        );
    }

    // Pacote responsavel pelas habilidades
    public static class ActionPacket {

        private final byte action;

        public ActionPacket(byte action) {
            this.action = action;
        }

        // Converte a acao em dados de rede
        public static void encode(
                ActionPacket packet,
                FriendlyByteBuf buffer) {

            buffer.writeByte(packet.action);
        }

        // Reconstrói a acao recebida
        public static ActionPacket decode(
                FriendlyByteBuf buffer) {

            return new ActionPacket(
                    buffer.readByte()
            );
        }

        // Processa a acao no servidor
        public static void handle(
                ActionPacket packet,
                Supplier<NetworkEvent.Context> supplier) {

            NetworkEvent.Context context = supplier.get();

            context.enqueueWork(() -> {

                ServerPlayer player = context.getSender();

                if (player == null) {
                    return;
                }

                // Verifica se os Seis Olhos estao equipados
                boolean equipped =
                        player.getItemBySlot(EquipmentSlot.HEAD)
                                .is(ModItems.SIX_EYES.get());

                if (!equipped) {

                    player.displayClientMessage(
                            Component.literal(
                                    "Equipe os Seis Olhos primeiro!"
                            ),
                            true
                    );

                    return;
                }

                // Envia o comando para o sistema de poderes
                GojoPowers.execute(player, packet.action);

            });

            context.setPacketHandled(true);
        }
    }
}
