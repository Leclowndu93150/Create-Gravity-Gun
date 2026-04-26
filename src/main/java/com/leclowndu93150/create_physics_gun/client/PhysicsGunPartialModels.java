package com.leclowndu93150.create_physics_gun.client;

import com.leclowndu93150.create_physics_gun.CreatePhysicsGun;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public final class PhysicsGunPartialModels {
    public static final PartialModel CORE = item("physics_gun/core");
    public static final PartialModel CORE_GLOW = item("physics_gun/core_glow");
    public static final PartialModel RING = item("physics_gun/ring");
    public static final PartialModel SIGMA = item("physics_gun/sigma");
    public static final PartialModel INNER_CUBE = item("physics_gun/inner_cube");
    public static final PartialModel OUTER_CUBE = item("physics_gun/outer_cube");

    private PhysicsGunPartialModels() {}

    public static void init() {}

    private static PartialModel item(final String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreatePhysicsGun.MODID, "item/" + path));
    }
}
