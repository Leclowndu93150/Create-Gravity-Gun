package com.leclowndu93150.create_physics_gun.client;

import com.leclowndu93150.create_physics_gun.CreatePhysicsGun;
import com.leclowndu93150.create_physics_gun.item.PhysicsGunItem;
import com.leclowndu93150.create_physics_gun.mixin.PhysicsBeamAccessor;
import com.leclowndu93150.create_physics_gun.mixin.PhysicsStaffClientHandlerAccessor;
import com.leclowndu93150.create_physics_gun.network.PhysicsGunSyncPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffClientHandler;
import net.createmod.catnip.render.DefaultSuperRenderTypeBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = CreatePhysicsGun.MODID, value = Dist.CLIENT)
public final class PhysicsGunClientVisuals {
    private static PhysicsGunSyncPacket current = PhysicsGunSyncPacket.none();
    private static PhysicsStaffClientHandler.PhysicsBeam entityBeam;
    private static float extension;
    private static float previousExtension;
    private static float cubeScale;
    private static float previousCubeScale;
    private static float tilt;
    private static float previousTilt;

    private PhysicsGunClientVisuals() {}

    public static void handleSync(final PhysicsGunSyncPacket packet) {
        if (packet.targetType() != current.targetType()
                || (packet.targetType() == PhysicsGunSyncPacket.TargetType.SUB_LEVEL && !packet.subLevelId().equals(current.subLevelId()))) {
            removeStaffBeam();
        }

        current = packet;
        if (packet.targetType() != PhysicsGunSyncPacket.TargetType.ENTITY) {
            entityBeam = null;
        }
    }

    public static boolean isGrabbing() {
        return current.targetType() != PhysicsGunSyncPacket.TargetType.NONE;
    }

    public static void clear() {
        if (current.targetType() != PhysicsGunSyncPacket.TargetType.NONE) {
            removeStaffBeam();
        }
        current = PhysicsGunSyncPacket.none();
        entityBeam = null;
    }

    public static void clientTick() {
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            clear();
            resetAnimation();
            return;
        }

        final boolean holdingGun = holdingGun(player);
        if (!holdingGun) {
            clear();
            resetAnimation();
            return;
        }

        final PhysicsStaffClientHandler handler = SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER;
        final boolean active = isGrabbing();

        previousExtension = extension;
        extension = Mth.lerp(0.65f, extension, active ? 1.0f : 0.0f);
        previousCubeScale = cubeScale;
        cubeScale = Mth.lerp(0.65f, cubeScale, active ? 1.0f : 0.0f);
        previousTilt = tilt;
        tilt = Mth.lerp(0.65f, tilt, active ? 1.0f : 0.0f);

        handler.previousExtension = previousExtension;
        handler.extension = extension;
        handler.targetExtension = active ? 1.0f : 0.0f;
        handler.previousCubeScale = previousCubeScale;
        handler.cubeScale = cubeScale;
        handler.previousTilt = previousTilt;
        handler.tilt = tilt;

        if (!active) {
            return;
        }

        final boolean mainHand = player.getMainHandItem().getItem() instanceof PhysicsGunItem;
        final Vec3 focusPos = PhysicsStaffClientHandler.getStaffFocusPos(player, mainHand, 1.0f);

        if (current.targetType() == PhysicsGunSyncPacket.TargetType.SUB_LEVEL) {
            final Vec3 localAnchor = new Vec3(current.x(), current.y(), current.z());
            PhysicsGunBeamTint.run(() -> handler.updateBeam(player.level(), player.getUUID(), focusPos, localAnchor));
            return;
        }

        final Vec3 end = entityCenter(minecraft);
        if (end == null) {
            clear();
            return;
        }

        if (entityBeam == null) {
            PhysicsGunBeamTint.run(() -> entityBeam = new PhysicsStaffClientHandler.PhysicsBeam(focusPos, end, focusPos.distanceTo(end)));
        }
        ((PhysicsBeamAccessor) entityBeam).cpg$update();
    }

    @SubscribeEvent
    public static void onRenderLevel(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || entityBeam == null || !isGrabbing()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null || !holdingGun(player)) {
            return;
        }

        final Vec3 end = entityCenter(minecraft);
        if (end == null) {
            clear();
            return;
        }

        final float pt = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        final boolean mainHand = player.getMainHandItem().getItem() instanceof PhysicsGunItem;
        final Vec3 focusPos = PhysicsStaffClientHandler.getStaffFocusPos(player, mainHand, pt);
        final Vec3 camera = event.getCamera().getPosition();
        final SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();

        ((PhysicsBeamAccessor) entityBeam).cpg$render(focusPos, end, event.getPoseStack(), buffer, camera, pt);
        buffer.draw();
        RenderSystem.enableCull();
    }

    private static Vec3 entityCenter(final Minecraft minecraft) {
        if (minecraft.level == null || current.targetType() != PhysicsGunSyncPacket.TargetType.ENTITY) {
            return null;
        }
        final Entity entity = minecraft.level.getEntity(current.entityId());
        if (entity == null || !entity.isAlive()) {
            return null;
        }
        return entity.getBoundingBox().getCenter();
    }

    private static boolean holdingGun(final LocalPlayer player) {
        final ItemStack main = player.getMainHandItem();
        final ItemStack off = player.getOffhandItem();
        return main.getItem() instanceof PhysicsGunItem || off.getItem() instanceof PhysicsGunItem;
    }

    private static void resetAnimation() {
        extension = 0.0f;
        previousExtension = 0.0f;
        cubeScale = 0.0f;
        previousCubeScale = 0.0f;
        tilt = 0.0f;
        previousTilt = 0.0f;
    }

    private static void removeStaffBeam() {
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        final PhysicsStaffClientHandler handler = SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER;
        ((PhysicsStaffClientHandlerAccessor) handler).cpg$beams().remove(player.getUUID());
    }

}
