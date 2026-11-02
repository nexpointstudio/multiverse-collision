package com.nexpoint.multiversecollision.power;

import com.nexpoint.multiversecollision.MultiverseCollision;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid = MultiverseCollision.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GojoPowers {
    private record Blue(Vec3 center, int ticks, boolean released) {}
    private record Shot(Vec3 pos, Vec3 dir, int ticks, boolean purple) {}
    private record Domain(Vec3 center, int ticks) {}
    private static final Map<UUID, Blue> BLUES = new HashMap<>();
    private static final Map<UUID, Shot> SHOTS = new HashMap<>();
    private static final Map<UUID, Domain> DOMAINS = new HashMap<>();

    public static void execute(ServerPlayer p, int action) {
        if (!GojoEnergy.hasSixEyes(p) || !p.isAlive() || p.isSpectator()) return;
        switch (action) {
            case 0 -> blue(p);
            case 1 -> releaseBlue(p);
            case 2 -> shoot(p, false);
            case 3 -> reverse(p);
            case 4 -> shoot(p, true);
            case 5 -> amplified(p);
            case 6 -> domain(p);
            case 7 -> infinity(p);
            default -> { }
        }
    }

    private static boolean pay(ServerPlayer p, GojoProgression.Skill skill, int cost, String cooldown, int ticks) {
        CompoundTag data = GojoEnergy.getData(p);
        long now = p.serverLevel().getGameTime();
        if (!GojoProgression.owns(p, skill)) { msg(p, "Compre " + skill.name().toLowerCase() + " com /gojo buy."); return false; }
        if (now < data.getLong(cooldown)) { msg(p, "Habilidade em recarga."); return false; }
        if (!GojoEnergy.consume(p, cost)) { msg(p, "Energia insuficiente."); return false; }
        data.putLong(cooldown, now + ticks);
        return true;
    }

    private static void blue(ServerPlayer p) {
        if (BLUES.containsKey(p.getUUID())) return;
        if (pay(p, GojoProgression.Skill.BLUE, GojoConfig.BLUE_COST.get(), "blue_cd", GojoConfig.BLUE_COOLDOWN.get()))
            BLUES.put(p.getUUID(), new Blue(p.getEyePosition().add(p.getLookAngle().scale(4)), 0, false));
    }
    private static void releaseBlue(ServerPlayer p) {
        Blue b = BLUES.get(p.getUUID());
        if (b != null && !b.released) BLUES.put(p.getUUID(), new Blue(b.center.add(p.getLookAngle().scale(4)), GojoConfig.BLUE_DURATION.get(), true));
    }
    private static void reverse(ServerPlayer p) {
        if (p.getHealth() < p.getMaxHealth() && pay(p, GojoProgression.Skill.REVERSE, GojoConfig.REVERSE_COST.get(), "reverse_cd", GojoConfig.REVERSE_COOLDOWN.get())) p.heal(8);
    }
    private static void infinity(ServerPlayer p) {
        CompoundTag d = GojoEnergy.getData(p);
        if (d.getBoolean("infinity")) { d.putBoolean("infinity", false); return; }
        if (GojoProgression.owns(p, GojoProgression.Skill.INFINITY) && GojoEnergy.getEnergy(p) >= GojoConfig.INFINITY_DRAIN.get() / 20)
            d.putBoolean("infinity", true);
        else msg(p, "Infinito bloqueado ou sem energia.");
    }
    private static void shoot(ServerPlayer p, boolean purple) {
        if (SHOTS.containsKey(p.getUUID())) return;
        if (pay(p, purple ? GojoProgression.Skill.PURPLE : GojoProgression.Skill.RED,
            purple ? GojoConfig.PURPLE_COST.get() : GojoConfig.RED_COST.get(), purple ? "purple_cd" : "red_cd",
            purple ? GojoConfig.PURPLE_COOLDOWN.get() : GojoConfig.RED_COOLDOWN.get()))
            SHOTS.put(p.getUUID(), new Shot(p.getEyePosition(), p.getLookAngle().normalize(),
                purple ? GojoConfig.PURPLE_RANGE.get() : GojoConfig.RED_RANGE.get(), purple));
    }
    private static Vec3 aim(ServerPlayer p, int range) {
        Vec3 start = p.getEyePosition(), end = start.add(p.getLookAngle().scale(range));
        BlockHitResult hit = p.serverLevel().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
    }
    private static void amplified(ServerPlayer p) {
        if (!pay(p, GojoProgression.Skill.AMPLIFIED, GojoConfig.AMPLIFIED_COST.get(), "amplified_cd", GojoConfig.AMPLIFIED_COOLDOWN.get())) return;
        Vec3 c = aim(p, GojoConfig.PURPLE_RANGE.get());
        GojoWorld.push(p, c, GojoConfig.AMPLIFIED_RADIUS.get(), 2.5, 35);
        GojoWorld.breakBlocks(p, c, GojoConfig.AMPLIFIED_RADIUS.get(), 64);
        GojoWorld.particles(p.serverLevel(), c, true);
    }
    private static void domain(ServerPlayer p) {
        if (DOMAINS.containsKey(p.getUUID())) return;
        if (pay(p, GojoProgression.Skill.DOMAIN, GojoConfig.DOMAIN_COST.get(), "domain_cd", GojoConfig.DOMAIN_COOLDOWN.get()))
            DOMAINS.put(p.getUUID(), new Domain(p.position(), GojoConfig.DOMAIN_DURATION.get()));
    }

    public static boolean domainContains(Entity entity) {
        if (entity.level().isClientSide()) return false;
        for (var entry : DOMAINS.entrySet()) {
            ServerPlayer owner = entity.getServer().getPlayerList().getPlayer(entry.getKey());
            if (owner == null || owner.level() != entity.level() || !GojoWorld.canAffect(owner, entity)) continue;
            if (entity.position().distanceToSqr(entry.getValue().center) <= Math.pow(GojoConfig.DOMAIN_RADIUS.get(), 2)) return true;
        }
        return false;
    }
    public static void cancel(ServerPlayer p) {
        BLUES.remove(p.getUUID()); SHOTS.remove(p.getUUID()); DOMAINS.remove(p.getUUID());
        GojoEnergy.getData(p).putBoolean("infinity", false);
    }
    public static boolean blueSustained(ServerPlayer p) {
        Blue b = BLUES.get(p.getUUID());
        return b != null && !b.released;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Iterator<Map.Entry<UUID, Blue>> blues = BLUES.entrySet().iterator();
        while (blues.hasNext()) {
            var e = blues.next(); ServerPlayer p = event.getServer().getPlayerList().getPlayer(e.getKey());
            if (invalid(p)) { blues.remove(); continue; }
            Blue b = e.getValue();
            if (!b.released) {
                if (!GojoEnergy.consume(p, GojoConfig.BLUE_DRAIN.get().floatValue() / 20)) { blues.remove(); continue; }
                b = new Blue(p.getEyePosition().add(p.getLookAngle().scale(4)), 0, false);
            } else if (b.ticks <= 0) { blues.remove(); continue; }
            else b = new Blue(b.center, b.ticks - 1, true);
            e.setValue(b); GojoWorld.pull(p, b.center, GojoConfig.BLUE_RADIUS.get());
            if (p.tickCount % 5 == 0) {
                GojoWorld.particles(p.serverLevel(), b.center, false);
                GojoWorld.liftBlocks(p, b.center, GojoConfig.BLOCKS_PER_TICK.get());
            }
        }
        Iterator<Map.Entry<UUID, Shot>> shots = SHOTS.entrySet().iterator();
        while (shots.hasNext()) {
            var e = shots.next(); ServerPlayer p = event.getServer().getPlayerList().getPlayer(e.getKey());
            if (invalid(p)) { shots.remove(); continue; }
            Shot s = e.getValue(); if (s.ticks <= 0) { shots.remove(); continue; }
            Vec3 next = s.pos.add(s.dir);
            BlockHitResult hit = p.serverLevel().clip(new ClipContext(s.pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
            Vec3 c = hit.getType() == HitResult.Type.MISS ? next : hit.getLocation();
            GojoWorld.push(p, c, s.purple ? 2.5 : 2, s.purple ? 0.55 : 1.5, s.purple ? 14 : 8);
            if (s.purple && p.tickCount % 2 == 0) GojoWorld.breakBlocks(p, c, 1, 2);
            if (hit.getType() != HitResult.Type.MISS) {
                GojoWorld.breakBlocks(p, c, s.purple ? 2 : 1, s.purple ? 10 : 3); shots.remove();
            } else e.setValue(new Shot(next, s.dir, s.ticks - 1, s.purple));
            GojoWorld.particles(p.serverLevel(), c, s.purple);
        }
        Iterator<Map.Entry<UUID, Domain>> domains = DOMAINS.entrySet().iterator();
        while (domains.hasNext()) {
            var e = domains.next(); ServerPlayer p = event.getServer().getPlayerList().getPlayer(e.getKey());
            if (invalid(p) || e.getValue().ticks <= 0) { domains.remove(); continue; }
            Domain d = e.getValue();
            for (Entity target : GojoWorld.entities(p, d.center, GojoConfig.DOMAIN_RADIUS.get())) {
                if (!(target instanceof LivingEntity)) continue;
                target.setDeltaMovement(Vec3.ZERO); target.hurtMarked = true;
                if (target instanceof net.minecraft.world.entity.Mob mob) { mob.setTarget(null); mob.getNavigation().stop(); }
            }
            if (p.tickCount % 10 == 0) GojoWorld.particles(p.serverLevel(), d.center, true);
            e.setValue(new Domain(d.center, d.ticks - 1));
        }
    }
    private static boolean invalid(ServerPlayer p) { return p == null || !p.isAlive() || !GojoEnergy.hasSixEyes(p); }
    private static void msg(ServerPlayer p, String text) { p.displayClientMessage(Component.literal(text), true); }
    private GojoPowers() {}
}
