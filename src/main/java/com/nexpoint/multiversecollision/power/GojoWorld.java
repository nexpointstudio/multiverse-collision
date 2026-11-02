package com.nexpoint.multiversecollision.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

import java.util.List;

/** Mutations in this class happen on the logical server only. */
public final class GojoWorld {
    public static boolean canAffect(ServerPlayer owner, Entity entity) {
        if (entity == owner || !entity.isAlive()) return false;
        if (entity instanceof Player player) return GojoConfig.PVP.get() && owner.canHarmPlayer(player);
        return true;
    }

    public static List<Entity> entities(ServerPlayer owner, Vec3 center, double radius) {
        return owner.serverLevel().getEntities(owner, new AABB(center, center).inflate(radius), e -> canAffect(owner, e));
    }

    public static void pull(ServerPlayer owner, Vec3 center, double radius) {
        int count = 0;
        for (Entity e : entities(owner, center, radius)) {
            if (++count > 48) break;
            Vec3 delta = center.subtract(e.position());
            double distance = Math.max(0.7, delta.length());
            e.setDeltaMovement(e.getDeltaMovement().scale(0.65).add(delta.normalize().scale(Math.min(0.65, 0.85 / distance + 0.08))));
            e.hurtMarked = true;
        }
    }

    public static void push(ServerPlayer owner, Vec3 center, double radius, double force, float damage) {
        int count = 0;
        for (Entity e : entities(owner, center, radius)) {
            if (++count > 64) break;
            Vec3 delta = e.position().subtract(center);
            if (delta.lengthSqr() < 0.01) delta = new Vec3(0, 1, 0);
            if (damage > 0 && e instanceof LivingEntity living) living.hurt(owner.damageSources().playerAttack(owner), damage);
            e.setDeltaMovement(e.getDeltaMovement().add(delta.normalize().scale(force).add(0, 0.3, 0)));
            e.hurtMarked = true;
        }
    }

    public static void particles(ServerLevel level, Vec3 point, boolean purple) {
        level.sendParticles(purple ? ParticleTypes.DRAGON_BREATH : ParticleTypes.END_ROD,
            point.x, point.y, point.z, 8, 0.25, 0.25, 0.25, 0.02);
    }

    private static boolean eligible(ServerPlayer owner, BlockPos pos) {
        ServerLevel level = owner.serverLevel();
        if (!GojoConfig.TERRAIN.get() || !level.hasChunkAt(pos) || !level.mayInteract(owner, pos)) return false;
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && !state.hasBlockEntity() && state.getDestroySpeed(level, pos) >= 0
            && state.getFluidState().isEmpty() && state.isCollisionShapeFullBlock(level, pos)
            && MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, owner)) == false;
    }

    /** A limited number of simple blocks become vanilla falling blocks; their source is removed without drops. */
    public static void liftBlocks(ServerPlayer owner, Vec3 center, int limit) {
        if (limit <= 0) return;
        ServerLevel level = owner.serverLevel();
        BlockPos origin = BlockPos.containing(center);
        int moved = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -2, -2), origin.offset(2, 2, 2))) {
            if (moved >= limit) break;
            BlockPos at = pos.immutable();
            if (!eligible(owner, at)) continue;
            BlockState state = level.getBlockState(at);
            FallingBlockEntity falling = FallingBlockEntity.fall(level, at, state);
            if (falling != null) {
                falling.setDeltaMovement(center.subtract(Vec3.atCenterOf(at)).normalize().scale(0.4).add(0, 0.25, 0));
                moved++;
            }
        }
    }

    /** Destroys a capped number of eligible blocks with normal drops and player break hooks. */
    public static void breakBlocks(ServerPlayer owner, Vec3 center, int radius, int limit) {
        if (limit <= 0) return;
        BlockPos origin = BlockPos.containing(center);
        int broken = 0, checked = 0;
        outer: for (int x = 0; x <= radius * 2; x++)
            for (int y = 0; y <= radius * 2; y++)
                for (int z = 0; z <= radius * 2; z++) {
                    if (broken >= limit || checked++ >= 4096) break outer;
                    BlockPos at = origin.offset(centerOffset(x), centerOffset(y), centerOffset(z));
                    if (at.distSqr(origin) > radius * radius || !eligible(owner, at)) continue;
                    if (owner.gameMode.destroyBlock(at)) broken++;
                }
    }

    private static int centerOffset(int index) {
        return index == 0 ? 0 : (index + 1) / 2 * (index % 2 == 0 ? -1 : 1);
    }

    private GojoWorld() {}
}
