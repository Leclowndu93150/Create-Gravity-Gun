package com.leclowndu93150.create_physics_gun.mixin;

import com.leclowndu93150.create_physics_gun.client.PhysicsGunBeamTint;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(PhysicsStaffClientHandler.PhysicsBeam.class)
public abstract class PhysicsBeamColorMixin {

    @ModifyConstant(method = "<init>",
            constant = @Constant(intValue = 0xffffff),
            slice = @Slice(
                    from = @At(value = "HEAD"),
                    to = @At(value = "RETURN")))
    private int create_physics_gun$tintBeam(final int original) {
        return PhysicsGunBeamTint.tinting() ? 0xff8a3a : original;
    }
}
