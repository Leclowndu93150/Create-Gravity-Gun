package com.leclowndu93150.create_gravity_gun.mixin;

import com.leclowndu93150.create_gravity_gun.item.GravityGunItem;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffClientHandler;
import dev.simulated_team.simulated.util.click_interactions.InteractCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PhysicsStaffClientHandler.PhysicsStaffMouseHandler.class)
public abstract class PhysicsStaffMouseHandlerMixin {

    @Inject(method = "onAttack", at = @At("HEAD"), cancellable = true)
    private void cpg$skipAttack(final int modifiers, final int action,
                                final KeyMapping leftKey,
                                final CallbackInfoReturnable<InteractCallback.Result> cir) {
        if (holdingPhysicsGun()) cir.setReturnValue(new InteractCallback.Result(false));
    }

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void cpg$skipUse(final int modifiers, final int action,
                             final KeyMapping rightKey,
                             final CallbackInfoReturnable<InteractCallback.Result> cir) {
        if (holdingPhysicsGun()) cir.setReturnValue(new InteractCallback.Result(false));
    }

    @Inject(method = "onMouseMove", at = @At("HEAD"), cancellable = true)
    private void cpg$skipMouseMove(final double yaw, final double pitch,
                                   final CallbackInfoReturnable<InteractCallback.Result> cir) {
        if (holdingPhysicsGun()) cir.setReturnValue(new InteractCallback.Result(false));
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void cpg$skipScroll(final double dx, final double dy,
                                final CallbackInfoReturnable<InteractCallback.Result> cir) {
        if (holdingPhysicsGun()) cir.setReturnValue(new InteractCallback.Result(false));
    }

    private static boolean holdingPhysicsGun() {
        final LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return false;
        return p.getMainHandItem().getItem() instanceof GravityGunItem
                || p.getOffhandItem().getItem() instanceof GravityGunItem;
    }
}
