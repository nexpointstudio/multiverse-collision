package com.nexpoint.multiversecollision.power;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.nexpoint.multiversecollision.MultiverseCollision;

import java.util.List;

@Mod.EventBusSubscriber(modid = MultiverseCollision.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GojoProgression {
    // Custos provisórios de compra, separados do XP vanilla.
    public enum Skill {
        INFINITY(20), BLUE(30), REVERSE(30), RED(45), PURPLE(70), AMPLIFIED(100), DOMAIN(120);
        public final int cost;
        Skill(int cost) { this.cost = cost; }
    }

    public static boolean owns(ServerPlayer player, Skill skill) {
        return GojoEnergy.getData(player).getBoolean("skill_" + skill.name());
    }

    private static boolean prerequisite(ServerPlayer player, Skill skill) {
        return switch (skill) {
            case RED -> owns(player, Skill.REVERSE);
            case PURPLE -> owns(player, Skill.BLUE) && owns(player, Skill.RED);
            case AMPLIFIED -> owns(player, Skill.PURPLE);
            case DOMAIN -> owns(player, Skill.PURPLE) && owns(player, Skill.INFINITY);
            default -> true;
        };
    }

    public static void addXp(ServerPlayer player, int amount) {
        if (amount <= 0) return;
        CompoundTag data = GojoEnergy.getData(player);
        data.putInt("xp", Math.min(1_000_000, data.getInt("xp") + amount));
    }

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("gojo")
            .then(Commands.literal("status").executes(ctx -> {
                ServerPlayer p = ctx.getSource().getPlayerOrException();
                CompoundTag d = GojoEnergy.getData(p);
                String skills = String.join(", ", List.of(Skill.values()).stream()
                    .map(s -> s.name().toLowerCase() + ":" + (owns(p, s) ? "sim" : s.cost + " XP")).toList());
                p.sendSystemMessage(Component.literal("Gojo XP: " + d.getInt("xp") + " | Energia: "
                    + Math.round(GojoEnergy.getEnergy(p)) + "/100 | " + skills));
                return 1;
            }))
            .then(Commands.literal("buy").then(Commands.argument("skill", StringArgumentType.word())
                .suggests((ctx, builder) -> { for (Skill s : Skill.values()) builder.suggest(s.name().toLowerCase()); return builder.buildFuture(); })
                .executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Skill s;
                    try { s = Skill.valueOf(StringArgumentType.getString(ctx, "skill").toUpperCase()); }
                    catch (IllegalArgumentException e) { p.sendSystemMessage(Component.literal("Habilidade desconhecida.")); return 0; }
                    CompoundTag d = GojoEnergy.getData(p);
                    if (owns(p, s) || !prerequisite(p, s) || d.getInt("xp") < s.cost) {
                        p.sendSystemMessage(Component.literal("Compra indisponível: confira XP, aquisição e pré-requisitos em /gojo status."));
                        return 0;
                    }
                    d.putInt("xp", d.getInt("xp") - s.cost);
                    d.putBoolean("skill_" + s.name(), true);
                    p.sendSystemMessage(Component.literal("Desbloqueado: " + s.name()));
                    return 1;
                })))
            .then(Commands.literal("debug").requires(source -> source.hasPermission(2))
                .then(Commands.literal("xp").then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100000))
                        .executes(ctx -> { addXp(EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount")); return 1; }))))
                .then(Commands.literal("energy").then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> { GojoEnergy.setEnergy(EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount")); return 1; }))))
                .then(Commands.literal("unlock").then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .executes(ctx -> {
                            try {
                                Skill s = Skill.valueOf(StringArgumentType.getString(ctx, "skill").toUpperCase());
                                GojoEnergy.getData(EntityArgument.getPlayer(ctx, "player")).putBoolean("skill_" + s.name(), true);
                                return 1;
                            } catch (IllegalArgumentException e) { return 0; }
                        }))))));
    }

    private GojoProgression() {}
}
