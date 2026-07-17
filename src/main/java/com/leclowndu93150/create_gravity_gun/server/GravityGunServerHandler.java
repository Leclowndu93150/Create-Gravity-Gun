package com.leclowndu93150.create_gravity_gun.server;

import com.leclowndu93150.create_gravity_gun.config.GravityGunConfig;
import com.leclowndu93150.create_gravity_gun.item.GravityGunItem;
import com.leclowndu93150.create_gravity_gun.network.GravityGunFeedbackPacket;
import com.leclowndu93150.create_gravity_gun.registry.GravityGunComponents;
import net.minecraft.world.item.ItemStack;
import com.leclowndu93150.create_gravity_gun.network.GravityGunMotionPacket;
import com.leclowndu93150.create_gravity_gun.network.GravityGunPacket;
import com.leclowndu93150.create_gravity_gun.network.GravityGunSyncPacket;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GravityGunServerHandler {
    public static final double TICKS_PER_SECOND = 20.0;
    public static final float ENTITY_PICK_RADIUS = 1.0f;
    public static final double ENTITY_SEARCH_RADIUS = 2.0;
    public static final double DEFAULT_HOLD_DISTANCE = 2.75;
    public static final double MAX_DISPLACEMENT = 4.0;
    public static final double CARRY_BASE_DISTANCE = 0.61;
    public static final double CARRY_PLAYER_RADIUS = 0.3;
    public static final double CARRY_RADIUS_SCALE = 2.0;
    public static final double MAX_ANGULAR_DELTA_V = Math.toRadians(360.0 * 10.0);

    private static double grabRange() { return GravityGunConfig.grabRange; }
    private static double minHold() { return GravityGunConfig.minHoldDistance; }
    private static double maxHold() { return GravityGunConfig.maxHoldDistance; }
    private static double carryMaxSpeed() { return GravityGunConfig.carryMaxSpeed; }
    private static double maxLinearDeltaV() { return carryMaxSpeed() * 2.0; }
    private static double puntVelocity() { return GravityGunConfig.puntVelocity; }
    private static double entityCarryMaxSpeed() { return carryMaxSpeed() / TICKS_PER_SECOND; }
    private static double entityMaxLinearDeltaV() { return maxLinearDeltaV() / TICKS_PER_SECOND; }
    private static double maxPickupMass() { return GravityGunConfig.maxPickupMass; }
    private static long denySoundCooldownTicks() { return GravityGunConfig.denySoundCooldownTicks; }

    private static final Map<UUID, GrabState> STATES = new Object2ObjectOpenHashMap<>();

    private GravityGunServerHandler() {}

    public static GrabState stateOf(final UUID uuid) {
        return STATES.computeIfAbsent(uuid, k -> new GrabState());
    }

    public static void clear(final UUID uuid) {
        STATES.remove(uuid);
    }

    public static void onPacket(final GravityGunPacket packet, final ServerPlayer player) {
        final GrabState state = stateOf(player.getUUID());
        switch (packet.action()) {
            case TOGGLE_GRAB -> handleToggle(player, state);
            case PUNT -> handlePunt(player, state);
            case ADJUST_DISTANCE -> {
                state.holdDistance = Math.max(minHold(), Math.min(maxHold(), state.holdDistance + packet.dy()));
            }
            case TOGGLE_SOUNDS -> toggleSounds(player);
        }
    }

    private static void toggleSounds(final ServerPlayer player) {
        final ItemStack stack = gunStack(player);
        if (stack == null) return;
        final boolean current = stack.getOrDefault(GravityGunComponents.SOUNDS_ENABLED.get(), Boolean.TRUE);
        stack.set(GravityGunComponents.SOUNDS_ENABLED.get(), !current);
    }

    private static ItemStack gunStack(final ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof GravityGunItem) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof GravityGunItem) return player.getOffhandItem();
        return null;
    }

    private static void handleToggle(final ServerPlayer player, final GrabState state) {
        if (state.isHolding()) {
            releaseGrabbed(player, state);
            state.clear();
            syncNone(player);
            sendFeedback(player, GravityGunFeedbackPacket.Kind.DROP);
            return;
        }
        final ServerLevel level = player.serverLevel();
        final Vec3 eye = player.getEyePosition();
        final Vec3 look = player.getLookAngle();

        final Entity entity = pickEntity(player, eye, look);
        if (entity != null) {
            state.grabbedEntity = entity;
            state.holdDistance = holdDistance(entity.getBoundingBox(), look);
            state.freshGrab = true;
            PacketDistributor.sendToPlayer(player, GravityGunSyncPacket.entity(entity.getId()));
            sendFeedback(player, GravityGunFeedbackPacket.Kind.PICKUP);
            return;
        }

        final ServerSubLevel sub = pickSubLevel(level, eye, look);
        if (sub != null) {
            final MassData mass = sub.getMassTracker();
            if (mass != null && !mass.isInvalid() && mass.getMass() > maxPickupMass()) {
                playDenySound(player, state);
                return;
            }
            state.grabbedSubLevel = sub;
            state.subLevelLocalAnchor.set(localCenterOfMass(sub));
            state.holdDistance = holdDistance(sub.boundingBox(), look);
            state.freshGrab = true;
            PacketDistributor.sendToPlayer(player, GravityGunSyncPacket.subLevel(sub.getUniqueId(),
                    state.subLevelLocalAnchor.x, state.subLevelLocalAnchor.y, state.subLevelLocalAnchor.z));
            sendFeedback(player, GravityGunFeedbackPacket.Kind.PICKUP);
            return;
        }

        playDenySound(player, state);
    }

    private static void playDenySound(final ServerPlayer player, final GrabState state) {
        final long now = player.serverLevel().getGameTime();
        if (now - state.lastDenyTick < denySoundCooldownTicks()) return;
        state.lastDenyTick = now;
        sendFeedback(player, GravityGunFeedbackPacket.Kind.TOO_HEAVY);
    }

    private static void sendFeedback(final ServerPlayer player, final GravityGunFeedbackPacket.Kind kind) {
        if (!soundsEnabled(player)) return;
        PacketDistributor.sendToPlayer(player, new GravityGunFeedbackPacket(kind));
    }

    private static boolean soundsEnabled(final ServerPlayer player) {
        final ItemStack stack = gunStack(player);
        if (stack == null) return true;
        return stack.getOrDefault(GravityGunComponents.SOUNDS_ENABLED.get(), Boolean.TRUE);
    }

    private static void handlePunt(final ServerPlayer player, final GrabState state) {
        final Vec3 look = player.getLookAngle();
        if (state.grabbedSubLevel != null && !state.grabbedSubLevel.isRemoved()) {
            puntSubLevel(player, state.grabbedSubLevel, look, puntVelocity());
            state.clear();
            syncNone(player);
            sendFeedback(player, GravityGunFeedbackPacket.Kind.LAUNCH);
            return;
        }
        if (state.grabbedEntity != null && state.grabbedEntity.isAlive()) {
            puntEntity(state.grabbedEntity, look, puntVelocity());
            state.clear();
            syncNone(player);
            sendFeedback(player, GravityGunFeedbackPacket.Kind.LAUNCH);
            return;
        }
        final Entity e = pickEntity(player, player.getEyePosition(), look);
        if (e != null) {
            shoveEntity(e, look, puntVelocity() * 0.5);
            sendFeedback(player, GravityGunFeedbackPacket.Kind.LAUNCH);
            return;
        }
        final SubLevelHit subHit = pickSubLevelHit(player.serverLevel(), player.getEyePosition(), look);
        if (subHit != null) {
            final MassData mass = subHit.sub().getMassTracker();
            if (mass != null && !mass.isInvalid() && mass.getMass() > maxPickupMass()) {
                playDenySound(player, state);
                return;
            }
            puntSubLevelOffCenter(player, subHit.sub(), subHit.hit(), look, puntVelocity() * 0.5);
            sendFeedback(player, GravityGunFeedbackPacket.Kind.LAUNCH);
            return;
        }
        sendFeedback(player, GravityGunFeedbackPacket.Kind.DRYFIRE);
    }

    public static void tickAll(final MinecraftServer server) {
        STATES.entrySet().removeIf(entry -> {
            final ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) return true;
            final GrabState s = entry.getValue();
            if (!isHoldingGun(player)) {
                final boolean wasHolding = s.isHolding();
                releaseGrabbed(player, s);
                s.clear();
                syncNone(player);
                if (wasHolding) sendFeedback(player, GravityGunFeedbackPacket.Kind.DROP);
                return false;
            }
            tickPlayer(player, s);
            return false;
        });
    }

    private static boolean isHoldingGun(final ServerPlayer player) {
        return player.getMainHandItem().getItem() instanceof GravityGunItem
                || player.getOffhandItem().getItem() instanceof GravityGunItem;
    }

    private static void tickPlayer(final ServerPlayer player, final GrabState state) {
        if (state.grabbedSubLevel != null && state.grabbedSubLevel.isRemoved()) {
            state.clear();
            syncNone(player);
            sendFeedback(player, GravityGunFeedbackPacket.Kind.DROP);
            return;
        }
        if (state.grabbedEntity != null && !state.grabbedEntity.isAlive()) {
            releaseGrabbed(player, state);
            state.clear();
            syncNone(player);
            sendFeedback(player, GravityGunFeedbackPacket.Kind.DROP);
            return;
        }

        final Vec3 eye = player.getEyePosition();
        final Vec3 look = player.getLookAngle();
        final Vec3 target = eye.add(look.scale(state.holdDistance));

        if (state.grabbedSubLevel != null) {
            pullSubLevel(player.serverLevel(), state.grabbedSubLevel, state.subLevelLocalAnchor, target, state.freshGrab);
        } else if (state.grabbedEntity != null) {
            pullEntity(state.grabbedEntity, target, state.freshGrab);
        }
        state.freshGrab = false;
    }

    private static void pullSubLevel(final ServerLevel level, final ServerSubLevel sub, final Vector3dc localAnchor, final Vec3 target, final boolean fresh) {
        final SubLevelPhysicsSystem system = SubLevelPhysicsSystem.get(level);
        if (system == null) return;
        final MassData mass = sub.getMassTracker();
        if (mass == null || mass.isInvalid()) return;

        final int bodyId = sub.getRuntimeId();
        if (bodyId == PhysicsPipelineBody.NULL_RUNTIME_ID) return;

        final RigidBodyHandle handle = RigidBodyHandle.of(sub);
        if (handle == null) return;

        final Vector3d anchor = sub.logicalPose().transformPosition(localAnchor, new Vector3d());

        final Vector3d linVel = handle.getLinearVelocity(new Vector3d());
        final Vector3d angVel = handle.getAngularVelocity(new Vector3d());

        if (fresh) {
            final Vector3d linearStop = clamp(new Vector3d(-linVel.x, -linVel.y, -linVel.z), maxLinearDeltaV());
            final Vector3d angularStop = clamp(new Vector3d(-angVel.x, -angVel.y, -angVel.z), MAX_ANGULAR_DELTA_V);
            handle.addLinearAndAngularVelocity(linearStop, angularStop);
            return;
        }

        final Vector3d desiredVelocity = desiredCarryVelocity(new Vec3(anchor.x, anchor.y, anchor.z), target);
        final Vector3d linearDelta = clamp(new Vector3d(
                desiredVelocity.x - linVel.x,
                desiredVelocity.y - linVel.y,
                desiredVelocity.z - linVel.z
        ), maxLinearDeltaV());
        final Vector3d angularDelta = clamp(new Vector3d(
                -angVel.x * 0.5,
                -angVel.y * 0.5,
                -angVel.z * 0.5
        ), MAX_ANGULAR_DELTA_V);

        handle.addLinearAndAngularVelocity(linearDelta, angularDelta);
    }

    private static void pullEntity(final Entity entity, final Vec3 target, final boolean fresh) {
        final Vec3 cur = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
        final Vec3 vel = entity.getDeltaMovement();

        if (fresh) {
            final Vec3 stop = clamp(vel.scale(-1.0), entityMaxLinearDeltaV());
            final Vec3 next = vel.add(stop);
            applyEntityVelocity(entity, next);
            return;
        }

        final Vec3 desiredVelocity = desiredEntityCarryVelocity(cur, target);
        final Vec3 delta = clamp(new Vec3(
                desiredVelocity.x - vel.x,
                desiredVelocity.y - vel.y,
                desiredVelocity.z - vel.z
        ), entityMaxLinearDeltaV());

        applyEntityVelocity(entity, vel.add(delta));
    }

    private static void applyEntityVelocity(final Entity entity, final Vec3 velocity) {
        entity.setDeltaMovement(velocity);
        entity.fallDistance = 0.0f;
        entity.hasImpulse = true;
        entity.hurtMarked = true;
        if (entity instanceof final ServerPlayer grabbed) {
            PacketDistributor.sendToPlayer(grabbed, GravityGunMotionPacket.of(velocity.x, velocity.y, velocity.z));
        }
    }

    private static void releaseGrabbed(final ServerPlayer holder, final GrabState state) {
        if (state.grabbedEntity instanceof final ServerPlayer grabbed) {
            PacketDistributor.sendToPlayer(grabbed, GravityGunMotionPacket.clear());
        }
    }

    private static void puntSubLevel(final ServerPlayer player, final ServerSubLevel sub, final Vec3 look, final double speed) {
        final ServerLevel level = player.serverLevel();
        final SubLevelPhysicsSystem system = SubLevelPhysicsSystem.get(level);
        if (system == null) return;
        final int bodyId = sub.getRuntimeId();
        if (bodyId == PhysicsPipelineBody.NULL_RUNTIME_ID) return;

        final RigidBodyHandle handle = RigidBodyHandle.of(sub);
        if (handle == null) return;

        handle.addLinearAndAngularVelocity(
                new Vector3d(look.x * speed, look.y * speed, look.z * speed),
                new Vector3d(0.0, 0.0, 0.0));
    }

    private static void puntSubLevelOffCenter(final ServerPlayer player, final ServerSubLevel sub, final Vec3 hitWorld,
                                              final Vec3 look, final double speed) {
        final ServerLevel level = player.serverLevel();
        final SubLevelPhysicsSystem system = SubLevelPhysicsSystem.get(level);
        if (system == null) return;
        final int bodyId = sub.getRuntimeId();
        if (bodyId == PhysicsPipelineBody.NULL_RUNTIME_ID) return;

        final RigidBodyHandle handle = RigidBodyHandle.of(sub);
        if (handle == null) return;

        final MassData mass = sub.getMassTracker();
        final double bodyMass = mass != null && !mass.isInvalid() ? Math.max(mass.getMass(), 1.0) : 10.0;

        final Vector3d point = new Vector3d(hitWorld.x, hitWorld.y, hitWorld.z);
        final double impulse = bodyMass * speed;
        final Vector3d force = new Vector3d(look.x, look.y, look.z).mul(impulse);

        handle.applyImpulseAtPoint(point, force);
    }

    private static void puntEntity(final Entity entity, final Vec3 look, final double speed) {
        final double tickSpeed = speed / TICKS_PER_SECOND;
        final Vec3 v = new Vec3(look.x * tickSpeed, look.y * tickSpeed + 0.2, look.z * tickSpeed);
        entity.setDeltaMovement(v);
        entity.fallDistance = 0.0f;
        entity.hasImpulse = true;
        entity.hurtMarked = true;
        if (entity instanceof final ServerPlayer grabbed) {
            PacketDistributor.sendToPlayer(grabbed, GravityGunMotionPacket.clear());
        }
    }

    private static void shoveEntity(final Entity entity, final Vec3 look, final double speed) {
        final double tickSpeed = speed / TICKS_PER_SECOND;
        final Vec3 v = new Vec3(look.x * tickSpeed, look.y * tickSpeed + 0.15, look.z * tickSpeed);
        entity.setDeltaMovement(entity.getDeltaMovement().add(v));
        entity.fallDistance = 0.0f;
        entity.hasImpulse = true;
        entity.hurtMarked = true;
        if (entity instanceof final ServerPlayer grabbed) {
            PacketDistributor.sendToPlayer(grabbed, GravityGunMotionPacket.clear());
        }
    }

    private static Entity pickEntity(final ServerPlayer player, final Vec3 eye, final Vec3 look) {
        final Vec3 end = eye.add(look.scale(grabRange()));
        final AABB box = new AABB(eye, end).inflate(ENTITY_SEARCH_RADIUS);
        final EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end, box,
                e -> e.isAlive() && e.isPickable() && e != player, ENTITY_PICK_RADIUS);
        return hit == null ? null : hit.getEntity();
    }

    private static ServerSubLevel pickSubLevel(final ServerLevel level, final Vec3 eye, final Vec3 look) {
        final SubLevelHit hit = pickSubLevelHit(level, eye, look);
        return hit == null ? null : hit.sub;
    }

    private static SubLevelHit pickSubLevelHit(final ServerLevel level, final Vec3 eye, final Vec3 look) {
        final Vec3 end = eye.add(look.scale(grabRange()));
        final BoundingBox3d aabb = new BoundingBox3d(
                Math.min(eye.x, end.x) - 2, Math.min(eye.y, end.y) - 2, Math.min(eye.z, end.z) - 2,
                Math.max(eye.x, end.x) + 2, Math.max(eye.y, end.y) + 2, Math.max(eye.z, end.z) + 2);

        ServerSubLevel best = null;
        Vec3 bestHit = null;
        double bestDist = Double.POSITIVE_INFINITY;
        for (final SubLevelAccess sub : SableCompanion.INSTANCE.getAllIntersecting(level, aabb)) {
            if (!(sub instanceof final ServerSubLevel server)) continue;
            final BoundingBox3dc bounds = server.boundingBox();
            final AABB box = new AABB(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ());
            final var hit = box.clip(eye, end);
            if (hit.isEmpty()) continue;
            final double d = hit.get().distanceToSqr(eye);
            if (d < bestDist) {
                bestDist = d;
                best = server;
                bestHit = hit.get();
            }
        }
        return best == null ? null : new SubLevelHit(best, bestHit);
    }

    private record SubLevelHit(ServerSubLevel sub, Vec3 hit) {}

    private static Vector3d localCenterOfMass(final ServerSubLevel sub) {
        final MassData mass = sub.getMassTracker();
        final Vector3dc centerOfMass = mass != null && !mass.isInvalid() ? mass.getCenterOfMass() : null;
        if (centerOfMass != null) {
            return new Vector3d(centerOfMass);
        }
        return new Vector3d(sub.logicalPose().rotationPoint());
    }

    private static Vector3d desiredCarryVelocity(final Vec3 current, final Vec3 target) {
        final Vec3 displacement = target.subtract(current);
        final double distance = Math.min(MAX_DISPLACEMENT, displacement.length());
        if (distance < 0.001) {
            return new Vector3d();
        }
        final double speed = Math.min(carryMaxSpeed(), distance * 20.0);
        final Vec3 velocity = displacement.normalize().scale(speed);
        return new Vector3d(velocity.x, velocity.y, velocity.z);
    }

    private static Vec3 desiredEntityCarryVelocity(final Vec3 current, final Vec3 target) {
        final Vec3 displacement = target.subtract(current);
        final double distance = Math.min(MAX_DISPLACEMENT, displacement.length());
        if (distance < 0.001) {
            return Vec3.ZERO;
        }
        final double speed = Math.min(entityCarryMaxSpeed(), distance);
        return displacement.normalize().scale(speed);
    }

    private static double holdDistance(final AABB box, final Vec3 look) {
        final double radius = CARRY_PLAYER_RADIUS + projectedRadius(
                (box.maxX - box.minX) * 0.5,
                (box.maxY - box.minY) * 0.5,
                (box.maxZ - box.minZ) * 0.5,
                look);
        return Math.max(minHold(), Math.min(maxHold(), CARRY_BASE_DISTANCE + radius * CARRY_RADIUS_SCALE));
    }

    private static double holdDistance(final BoundingBox3dc box, final Vec3 look) {
        final double radius = CARRY_PLAYER_RADIUS + projectedRadius(
                (box.maxX() - box.minX()) * 0.5,
                (box.maxY() - box.minY()) * 0.5,
                (box.maxZ() - box.minZ()) * 0.5,
                look);
        return Math.max(minHold(), Math.min(maxHold(), CARRY_BASE_DISTANCE + radius * CARRY_RADIUS_SCALE));
    }

    private static double projectedRadius(final double halfX, final double halfY, final double halfZ, final Vec3 look) {
        return Math.abs(look.x) * halfX + Math.abs(look.y) * halfY + Math.abs(look.z) * halfZ;
    }

    private static void syncNone(final ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, GravityGunSyncPacket.none());
    }

    private static Vector3d clamp(final Vector3d vector, final double maxLength) {
        final double length = vector.length();
        if (length > maxLength) {
            vector.mul(maxLength / length);
        }
        return vector;
    }

    private static Vec3 clamp(final Vec3 vector, final double maxLength) {
        final double length = vector.length();
        if (length > maxLength) {
            return vector.scale(maxLength / length);
        }
        return vector;
    }
}
