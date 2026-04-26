package com.leclowndu93150.create_physics_gun.server;

import com.leclowndu93150.create_physics_gun.item.PhysicsGunItem;
import com.leclowndu93150.create_physics_gun.network.PhysicsGunMotionPacket;
import com.leclowndu93150.create_physics_gun.network.PhysicsGunPacket;
import com.leclowndu93150.create_physics_gun.network.PhysicsGunSyncPacket;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.physics.impl.rapier.Rapier3D;
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

public final class PhysicsGunServerHandler {
    public static final double TICKS_PER_SECOND = 20.0;
    public static final double GRAB_RANGE = 24.0;
    public static final float ENTITY_PICK_RADIUS = 1.0f;
    public static final double ENTITY_SEARCH_RADIUS = 2.0;
    public static final double DEFAULT_HOLD_DISTANCE = 2.75;
    public static final double MIN_HOLD = 1.5;
    public static final double MAX_HOLD = 12.0;
    public static final double MAX_DISPLACEMENT = 4.0;
    public static final double CARRY_BASE_DISTANCE = 0.61;
    public static final double CARRY_PLAYER_RADIUS = 0.3;
    public static final double CARRY_RADIUS_SCALE = 2.0;
    public static final double CARRY_MAX_SPEED = 25.4;
    public static final double MAX_LINEAR_DELTA_V = CARRY_MAX_SPEED * 2.0;
    public static final double MAX_ANGULAR_DELTA_V = Math.toRadians(360.0 * 10.0);
    public static final double PUNT_VELOCITY = 38.1;
    public static final double ENTITY_CARRY_MAX_SPEED = CARRY_MAX_SPEED / TICKS_PER_SECOND;
    public static final double ENTITY_MAX_LINEAR_DELTA_V = MAX_LINEAR_DELTA_V / TICKS_PER_SECOND;

    private static final Map<UUID, GrabState> STATES = new Object2ObjectOpenHashMap<>();

    private PhysicsGunServerHandler() {}

    public static GrabState stateOf(final UUID uuid) {
        return STATES.computeIfAbsent(uuid, k -> new GrabState());
    }

    public static void clear(final UUID uuid) {
        STATES.remove(uuid);
    }

    public static void onPacket(final PhysicsGunPacket packet, final ServerPlayer player) {
        final GrabState state = stateOf(player.getUUID());
        switch (packet.action()) {
            case TOGGLE_GRAB -> handleToggle(player, state);
            case PUNT -> handlePunt(player, state);
            case ADJUST_DISTANCE -> {
                state.holdDistance = Math.max(MIN_HOLD, Math.min(MAX_HOLD, state.holdDistance + packet.dy()));
            }
        }
    }

    private static void handleToggle(final ServerPlayer player, final GrabState state) {
        if (state.isHolding()) {
            releaseGrabbed(state);
            state.clear();
            syncNone(player);
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
            PacketDistributor.sendToPlayer(player, PhysicsGunSyncPacket.entity(entity.getId()));
            return;
        }

        final ServerSubLevel sub = pickSubLevel(level, eye, look);
        if (sub != null) {
            state.grabbedSubLevel = sub;
            state.subLevelLocalAnchor.set(localCenterOfMass(sub));
            state.holdDistance = holdDistance(sub.boundingBox(), look);
            state.freshGrab = true;
            PacketDistributor.sendToPlayer(player, PhysicsGunSyncPacket.subLevel(sub.getUniqueId(),
                    state.subLevelLocalAnchor.x, state.subLevelLocalAnchor.y, state.subLevelLocalAnchor.z));
        }
    }

    private static void handlePunt(final ServerPlayer player, final GrabState state) {
        final Vec3 look = player.getLookAngle();
        if (state.grabbedSubLevel != null && !state.grabbedSubLevel.isRemoved()) {
            puntSubLevel(player, state.grabbedSubLevel, look, PUNT_VELOCITY);
            state.clear();
            syncNone(player);
            return;
        }
        if (state.grabbedEntity != null && state.grabbedEntity.isAlive()) {
            puntEntity(state.grabbedEntity, look, PUNT_VELOCITY);
            state.clear();
            syncNone(player);
            return;
        }
        final Entity e = pickEntity(player, player.getEyePosition(), look);
        if (e != null) {
            puntEntity(e, look, PUNT_VELOCITY * 0.5);
            return;
        }
        final ServerSubLevel sub = pickSubLevel(player.serverLevel(), player.getEyePosition(), look);
        if (sub != null) {
            puntSubLevel(player, sub, look, PUNT_VELOCITY * 0.5);
        }
    }

    public static void tickAll(final MinecraftServer server) {
        STATES.entrySet().removeIf(entry -> {
            final ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) return true;
            final GrabState s = entry.getValue();
            if (!isHoldingGun(player)) {
                releaseGrabbed(s);
                s.clear();
                syncNone(player);
                return false;
            }
            tickPlayer(player, s);
            return false;
        });
    }

    private static boolean isHoldingGun(final ServerPlayer player) {
        return player.getMainHandItem().getItem() instanceof PhysicsGunItem
                || player.getOffhandItem().getItem() instanceof PhysicsGunItem;
    }

    private static void tickPlayer(final ServerPlayer player, final GrabState state) {
        if (state.grabbedSubLevel != null && state.grabbedSubLevel.isRemoved()) {
            state.clear();
            syncNone(player);
            return;
        }
        if (state.grabbedEntity != null && !state.grabbedEntity.isAlive()) {
            releaseGrabbed(state);
            state.clear();
            syncNone(player);
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

        final int sceneId = Rapier3D.getID(level);
        final int bodyId = sub.getRuntimeId();
        if (bodyId == PhysicsPipelineBody.NULL_RUNTIME_ID) return;

        final Vector3d anchor = sub.logicalPose().transformPosition(localAnchor, new Vector3d());

        final double[] linVel = new double[3];
        final double[] angVel = new double[3];
        Rapier3D.getLinearVelocity(sceneId, bodyId, linVel);
        Rapier3D.getAngularVelocity(sceneId, bodyId, angVel);

        if (fresh) {
            final Vector3d linearStop = clamp(new Vector3d(-linVel[0], -linVel[1], -linVel[2]), MAX_LINEAR_DELTA_V);
            final Vector3d angularStop = clamp(new Vector3d(-angVel[0], -angVel[1], -angVel[2]), MAX_ANGULAR_DELTA_V);
            Rapier3D.addLinearAngularVelocities(sceneId, bodyId,
                    linearStop.x, linearStop.y, linearStop.z,
                    angularStop.x, angularStop.y, angularStop.z,
                    true);
            return;
        }

        final Vector3d desiredVelocity = desiredCarryVelocity(new Vec3(anchor.x, anchor.y, anchor.z), target);
        final Vector3d linearDelta = clamp(new Vector3d(
                desiredVelocity.x - linVel[0],
                desiredVelocity.y - linVel[1],
                desiredVelocity.z - linVel[2]
        ), MAX_LINEAR_DELTA_V);
        final Vector3d angularDelta = clamp(new Vector3d(
                -angVel[0] * 0.5,
                -angVel[1] * 0.5,
                -angVel[2] * 0.5
        ), MAX_ANGULAR_DELTA_V);

        Rapier3D.addLinearAngularVelocities(sceneId, bodyId,
                linearDelta.x, linearDelta.y, linearDelta.z,
                angularDelta.x, angularDelta.y, angularDelta.z,
                true);
    }

    private static void pullEntity(final Entity entity, final Vec3 target, final boolean fresh) {
        final Vec3 cur = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
        final Vec3 vel = entity.getDeltaMovement();

        if (fresh) {
            final Vec3 stop = clamp(vel.scale(-1.0), ENTITY_MAX_LINEAR_DELTA_V);
            final Vec3 next = vel.add(stop);
            applyEntityVelocity(entity, next);
            return;
        }

        final Vec3 desiredVelocity = desiredEntityCarryVelocity(cur, target);
        final Vec3 delta = clamp(new Vec3(
                desiredVelocity.x - vel.x,
                desiredVelocity.y - vel.y,
                desiredVelocity.z - vel.z
        ), ENTITY_MAX_LINEAR_DELTA_V);

        applyEntityVelocity(entity, vel.add(delta));
    }

    private static void applyEntityVelocity(final Entity entity, final Vec3 velocity) {
        entity.setDeltaMovement(velocity);
        entity.fallDistance = 0.0f;
        entity.hasImpulse = true;
        entity.hurtMarked = true;
        if (entity instanceof final ServerPlayer grabbed) {
            PacketDistributor.sendToPlayer(grabbed, PhysicsGunMotionPacket.of(velocity.x, velocity.y, velocity.z));
        }
    }

    private static void releaseGrabbed(final GrabState state) {
        if (state.grabbedEntity instanceof final ServerPlayer grabbed) {
            PacketDistributor.sendToPlayer(grabbed, PhysicsGunMotionPacket.clear());
        }
    }

    private static void puntSubLevel(final ServerPlayer player, final ServerSubLevel sub, final Vec3 look, final double speed) {
        final ServerLevel level = player.serverLevel();
        final SubLevelPhysicsSystem system = SubLevelPhysicsSystem.get(level);
        if (system == null) return;
        final int sceneId = Rapier3D.getID(level);
        final int bodyId = sub.getRuntimeId();
        if (bodyId == PhysicsPipelineBody.NULL_RUNTIME_ID) return;

        Rapier3D.addLinearAngularVelocities(sceneId, bodyId,
                look.x * speed, look.y * speed, look.z * speed,
                0.0, 0.0, 0.0, true);
    }

    private static void puntEntity(final Entity entity, final Vec3 look, final double speed) {
        final double tickSpeed = speed / TICKS_PER_SECOND;
        final Vec3 v = new Vec3(look.x * tickSpeed, look.y * tickSpeed + 0.2, look.z * tickSpeed);
        entity.setDeltaMovement(v);
        entity.fallDistance = 0.0f;
        entity.hasImpulse = true;
        entity.hurtMarked = true;
        if (entity instanceof final ServerPlayer grabbed) {
            PacketDistributor.sendToPlayer(grabbed, PhysicsGunMotionPacket.clear());
        }
    }

    private static Entity pickEntity(final ServerPlayer player, final Vec3 eye, final Vec3 look) {
        final Vec3 end = eye.add(look.scale(GRAB_RANGE));
        final AABB box = new AABB(eye, end).inflate(ENTITY_SEARCH_RADIUS);
        final EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end, box,
                e -> e.isAlive() && e.isPickable() && e != player, ENTITY_PICK_RADIUS);
        return hit == null ? null : hit.getEntity();
    }

    private static ServerSubLevel pickSubLevel(final ServerLevel level, final Vec3 eye, final Vec3 look) {
        final Vec3 end = eye.add(look.scale(GRAB_RANGE));
        final BoundingBox3d aabb = new BoundingBox3d(
                Math.min(eye.x, end.x) - 2, Math.min(eye.y, end.y) - 2, Math.min(eye.z, end.z) - 2,
                Math.max(eye.x, end.x) + 2, Math.max(eye.y, end.y) + 2, Math.max(eye.z, end.z) + 2);

        ServerSubLevel best = null;
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
            }
        }
        return best;
    }

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
        final double speed = Math.min(CARRY_MAX_SPEED, distance * 20.0);
        final Vec3 velocity = displacement.normalize().scale(speed);
        return new Vector3d(velocity.x, velocity.y, velocity.z);
    }

    private static Vec3 desiredEntityCarryVelocity(final Vec3 current, final Vec3 target) {
        final Vec3 displacement = target.subtract(current);
        final double distance = Math.min(MAX_DISPLACEMENT, displacement.length());
        if (distance < 0.001) {
            return Vec3.ZERO;
        }
        final double speed = Math.min(ENTITY_CARRY_MAX_SPEED, distance);
        return displacement.normalize().scale(speed);
    }

    private static double holdDistance(final AABB box, final Vec3 look) {
        final double radius = CARRY_PLAYER_RADIUS + projectedRadius(
                (box.maxX - box.minX) * 0.5,
                (box.maxY - box.minY) * 0.5,
                (box.maxZ - box.minZ) * 0.5,
                look);
        return Math.max(MIN_HOLD, Math.min(MAX_HOLD, CARRY_BASE_DISTANCE + radius * CARRY_RADIUS_SCALE));
    }

    private static double holdDistance(final BoundingBox3dc box, final Vec3 look) {
        final double radius = CARRY_PLAYER_RADIUS + projectedRadius(
                (box.maxX() - box.minX()) * 0.5,
                (box.maxY() - box.minY()) * 0.5,
                (box.maxZ() - box.minZ()) * 0.5,
                look);
        return Math.max(MIN_HOLD, Math.min(MAX_HOLD, CARRY_BASE_DISTANCE + radius * CARRY_RADIUS_SCALE));
    }

    private static double projectedRadius(final double halfX, final double halfY, final double halfZ, final Vec3 look) {
        return Math.abs(look.x) * halfX + Math.abs(look.y) * halfY + Math.abs(look.z) * halfZ;
    }

    private static void syncNone(final ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, PhysicsGunSyncPacket.none());
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
