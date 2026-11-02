package com.nexpoint.multiversecollision.power;

import com.nexpoint.multiversecollision.MultiverseCollision;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = MultiverseCollision.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GojoEvents {
    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer p)) return;
        CompoundTag d = GojoEnergy.getData(p);
        if (!GojoEnergy.hasSixEyes(p)) { GojoPowers.cancel(p); return; }
        if (d.getBoolean("infinity")) {
            if (!GojoProgression.owns(p, GojoProgression.Skill.INFINITY)
                || !GojoEnergy.consume(p, GojoConfig.INFINITY_DRAIN.get().floatValue() / 20)) d.putBoolean("infinity", false);
            else {
                p.setAirSupply(p.getMaxAirSupply()); p.clearFire();
                for (MobEffectInstance effect : List.copyOf(p.getActiveEffects()))
                    if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) p.removeEffect(effect.getEffect());
            }
        } else if (!GojoPowers.blueSustained(p)) GojoEnergy.regenerate(p);
        // One action-bar update every half second. This is also a server-to-client status sync.
        if (p.tickCount % 10 == 0) showStatus(p);
    }

    private static void showStatus(ServerPlayer p) {
        String status = "Energia: " + Math.round(GojoEnergy.getEnergy(p)) + "/100 | XP Gojo: "
            + GojoEnergy.getData(p).getInt("xp") + " | Infinito: " + (GojoEnergy.isInfinityActive(p) ? "ON" : "OFF");
        int range = GojoConfig.SIX_EYES_RANGE.get();
        List<Monster> enemies = p.serverLevel().getEntitiesOfClass(Monster.class,
            p.getBoundingBox().inflate(range), Monster::isAlive);
        enemies.sort(Comparator.comparingDouble(p::distanceToSqr));
        if (!enemies.isEmpty()) {
            Monster target = enemies.get(0);
            status += " | " + target.getName().getString() + " " + Math.round(target.getHealth())
                + "/" + Math.round(target.getMaxHealth()) + " HP, " + target.getArmorValue()
                + " arm., " + Math.round(p.distanceTo(target)) + "m";
            if (!target.getActiveEffects().isEmpty())
                status += " | Efeitos: " + target.getActiveEffects().stream().limit(3)
                    .map(e -> e.getEffect().getDisplayName().getString()).reduce((a, b) -> a + ", " + b).orElse("");
        }
        p.displayClientMessage(Component.literal(status), true);
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && GojoEnergy.isInfinityActive(p)
            && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) event.setCanceled(true);
        if (event.getSource().getEntity() != null && GojoPowers.domainContains(event.getSource().getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && GojoEnergy.isInfinityActive(p)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBreathe(LivingBreatheEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && GojoEnergy.isInfinityActive(p)) {
            event.setCanBreathe(true); event.setCanRefillAir(true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedEntities().removeIf(entity -> entity instanceof ServerPlayer p && GojoEnergy.isInfinityActive(p));
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        if (!GojoEnergy.hasSixEyes(killer) || mob.getPersistentData().getBoolean("gojo_xp_paid")) return;
        mob.getPersistentData().putBoolean("gojo_xp_paid", true);
        double attack = mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
        int xp = (int) Math.min(40, Math.max(1, mob.getMaxHealth() / 5 + attack / 2));
        if (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
            || mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss) xp = 60;
        GojoProgression.addXp(killer, xp);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        CompoundTag original = event.getOriginal().getPersistentData();
        if (original.contains(GojoEnergy.DATA_KEY)) event.getEntity().getPersistentData()
            .put(GojoEnergy.DATA_KEY, original.getCompound(GojoEnergy.DATA_KEY).copy());
        GojoEnergy.getData(event.getEntity()).putBoolean("infinity", false);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) GojoPowers.cancel(p);
    }

    private GojoEvents() {}
}
