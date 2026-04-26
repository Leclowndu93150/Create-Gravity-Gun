package com.leclowndu93150.create_physics_gun.mixin;

import com.leclowndu93150.create_physics_gun.item.PhysicsGunItem;
import com.leclowndu93150.create_physics_gun.client.PhysicsGunPartialModels;
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
    private BakedModel create_physics_gun$swapPartial(final BakedModel original, @Local(argsOnly = true) final ItemStack stack) {
        if (!(stack.getItem() instanceof PhysicsGunItem)) {
            return original;
        }
        final PartialModel ours = mapPartial(original);
        return ours == null ? original : ours.get();
    }

    private static PartialModel mapPartial(final BakedModel original) {
        if (original == SimPartialModels.PHYSICS_STAFF_CORE.get()) return PhysicsGunPartialModels.CORE;
        if (original == SimPartialModels.PHYSICS_STAFF_CORE_GLOW.get()) return PhysicsGunPartialModels.CORE_GLOW;
        if (original == SimPartialModels.PHYSICS_STAFF_RING.get()) return PhysicsGunPartialModels.RING;
        if (original == SimPartialModels.PHYSICS_STAFF_SIGMA.get()) return PhysicsGunPartialModels.SIGMA;
        if (original == SimPartialModels.PHYSICS_STAFF_INNER_CUBE.get()) return PhysicsGunPartialModels.INNER_CUBE;
        if (original == SimPartialModels.PHYSICS_STAFF_OUTER_CUBE.get()) return PhysicsGunPartialModels.OUTER_CUBE;
        return null;
    }
}
