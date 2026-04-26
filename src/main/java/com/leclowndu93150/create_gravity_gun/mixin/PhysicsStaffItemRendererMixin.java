package com.leclowndu93150.create_gravity_gun.mixin;

import com.leclowndu93150.create_gravity_gun.item.GravityGunItem;
import com.leclowndu93150.create_gravity_gun.client.GravityGunPartialModels;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffItemRenderer;
import dev.simulated_team.simulated.index.SimPartialModels;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PhysicsStaffItemRenderer.class)
public abstract class PhysicsStaffItemRendererMixin {

    @ModifyExpressionValue(method = "render",
            at = @At(value = "INVOKE",
                     target = "Ldev/engine_room/flywheel/lib/model/baked/PartialModel;get()Lnet/minecraft/client/resources/model/BakedModel;"))
    private BakedModel create_gravity_gun$swapPartial(final BakedModel original, @Local(argsOnly = true) final ItemStack stack) {
        if (!(stack.getItem() instanceof GravityGunItem)) {
            return original;
        }
        final PartialModel ours = mapPartial(original);
        return ours == null ? original : ours.get();
    }

    private static PartialModel mapPartial(final BakedModel original) {
        if (original == SimPartialModels.PHYSICS_STAFF_CORE.get()) return GravityGunPartialModels.CORE;
        if (original == SimPartialModels.PHYSICS_STAFF_CORE_GLOW.get()) return GravityGunPartialModels.CORE_GLOW;
        if (original == SimPartialModels.PHYSICS_STAFF_RING.get()) return GravityGunPartialModels.RING;
        if (original == SimPartialModels.PHYSICS_STAFF_SIGMA.get()) return GravityGunPartialModels.SIGMA;
        if (original == SimPartialModels.PHYSICS_STAFF_INNER_CUBE.get()) return GravityGunPartialModels.INNER_CUBE;
        if (original == SimPartialModels.PHYSICS_STAFF_OUTER_CUBE.get()) return GravityGunPartialModels.OUTER_CUBE;
        return null;
    }
}
