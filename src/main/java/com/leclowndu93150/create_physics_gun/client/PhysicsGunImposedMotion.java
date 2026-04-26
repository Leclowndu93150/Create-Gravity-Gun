package com.leclowndu93150.create_physics_gun.client;

import com.leclowndu93150.create_physics_gun.CreatePhysicsGun;
import com.leclowndu93150.create_physics_gun.network.PhysicsGunMotionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CreatePhysicsGun.MODID, value = Dist.CLIENT)
public final class PhysicsGunImposedMotion {
    private static volatile boolean active;
    private static volatile double vx;
    private static volatile double vy;
    private static volatile double vz;
    private static volatile long lastUpdateTick;

    private PhysicsGunImposedMotion() {}

    public static void apply(final PhysicsGunMotionPacket packet) {
        active = packet.active();
        vx = packet.vx();
        vy = packet.vy();
        vz = packet.vz();
        final Minecraft minecraft = Minecraft.getInstance();
        lastUpdateTick = minecraft.level == null ? 0 : minecraft.level.getGameTime();
    }

    public static boolean isActive() {
        return active;
    }

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Pre event) {
        if (!active) return;
        if (!(event.getEntity() instanceof final LocalPlayer player)) return;
        if (player.level().getGameTime() - lastUpdateTick > 5) {
            active = false;
            return;
        }
        player.setDeltaMovement(new Vec3(vx, vy, vz));
        player.fallDistance = 0.0f;
        player.hasImpulse = true;
    }
}
