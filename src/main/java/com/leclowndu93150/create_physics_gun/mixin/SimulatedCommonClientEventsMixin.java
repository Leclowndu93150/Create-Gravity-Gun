package com.leclowndu93150.create_physics_gun.mixin;

import com.leclowndu93150.create_physics_gun.client.PhysicsGunClientVisuals;
import dev.simulated_team.simulated.events.SimulatedCommonClientEvents;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimulatedCommonClientEvents.class)
public abstract class SimulatedCommonClientEventsMixin {
    @Inject(method = "postClientTick", at = @At("RETURN"))
    private static void cpg$tickPhysicsGunVisuals(final Minecraft instance, final CallbackInfo ci) {
        PhysicsGunClientVisuals.clientTick();
    }
}
