package com.leclowndu93150.create_gravity_gun.client;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = CreateGravityGun.MODID, value = Dist.CLIENT)
public final class GravityGunViewKick {
    private static final float DECAY = 0.78f;
    private static final float STIFFNESS = 0.45f;

    private static float pitchOffset;
    private static float pitchVelocity;
    private static float rollOffset;
    private static float rollVelocity;

    private static float previousPitchOffset;
    private static float previousRollOffset;

    private GravityGunViewKick() {}

    public static void punch(final float pitchKick, final float rollKick) {
        pitchVelocity += pitchKick;
        rollVelocity += rollKick;
    }

    @SubscribeEvent
    public static void onCameraAngles(final ViewportEvent.ComputeCameraAngles event) {
        final float pt = (float) event.getPartialTick();
        final float pitch = Mth.lerp(pt, previousPitchOffset, pitchOffset);
        final float roll = Mth.lerp(pt, previousRollOffset, rollOffset);
        event.setPitch(event.getPitch() + pitch);
        event.setRoll(event.getRoll() + roll);
    }

    public static void tick() {
        previousPitchOffset = pitchOffset;
        previousRollOffset = rollOffset;
        pitchOffset += pitchVelocity;
        rollOffset += rollVelocity;
        pitchVelocity = (pitchVelocity - pitchOffset * STIFFNESS) * DECAY;
        rollVelocity = (rollVelocity - rollOffset * STIFFNESS) * DECAY;
        if (Math.abs(pitchVelocity) < 0.01f && Math.abs(pitchOffset) < 0.01f) {
            pitchVelocity = 0;
            pitchOffset = 0;
        }
        if (Math.abs(rollVelocity) < 0.01f && Math.abs(rollOffset) < 0.01f) {
            rollVelocity = 0;
            rollOffset = 0;
        }
    }
}
