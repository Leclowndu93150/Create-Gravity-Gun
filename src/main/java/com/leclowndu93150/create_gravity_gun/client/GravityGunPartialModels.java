package com.leclowndu93150.create_gravity_gun.client;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public final class GravityGunPartialModels {
    public static final PartialModel CORE = item("gravity_gun/core");
    public static final PartialModel CORE_GLOW = item("gravity_gun/core_glow");
    public static final PartialModel RING = item("gravity_gun/ring");
    public static final PartialModel SIGMA = item("gravity_gun/sigma");
    public static final PartialModel INNER_CUBE = item("gravity_gun/inner_cube");
    public static final PartialModel OUTER_CUBE = item("gravity_gun/outer_cube");

    private GravityGunPartialModels() {}

    public static void init() {}

    private static PartialModel item(final String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(CreateGravityGun.MODID, "item/" + path));
    }
}
