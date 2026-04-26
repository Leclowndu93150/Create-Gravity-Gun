package com.leclowndu93150.create_gravity_gun.mixin;

import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffClientHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(PhysicsStaffClientHandler.class)
public interface PhysicsStaffClientHandlerAccessor {
    @Accessor("beams")
    Object2ObjectMap<UUID, PhysicsStaffClientHandler.PhysicsBeam> cpg$beams();
}
