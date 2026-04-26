package com.leclowndu93150.create_physics_gun;

import com.leclowndu93150.create_physics_gun.registry.PhysicsGunRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CreatePhysicsGun.MODID)
public class CreatePhysicsGun {
    public static final String MODID = "create_physics_gun";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreatePhysicsGun(final IEventBus modEventBus) {
        PhysicsGunRegistry.register(modEventBus);
        LOGGER.info("Create: Physics Gun loaded");
    }
}
