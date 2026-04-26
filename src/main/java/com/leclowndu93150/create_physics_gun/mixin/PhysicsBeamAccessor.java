package com.leclowndu93150.create_physics_gun.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffClientHandler;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PhysicsStaffClientHandler.PhysicsBeam.class)
public interface PhysicsBeamAccessor {
    @Invoker("update")
    void cpg$update();

    @Invoker("render")
    void cpg$render(Vec3 start, Vec3 end, PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt);
}
