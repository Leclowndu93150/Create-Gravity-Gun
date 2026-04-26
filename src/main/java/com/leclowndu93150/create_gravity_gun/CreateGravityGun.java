package com.leclowndu93150.create_gravity_gun;

import com.leclowndu93150.create_gravity_gun.registry.GravityGunRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CreateGravityGun.MODID)
public class CreateGravityGun {
    public static final String MODID = "create_gravity_gun";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateGravityGun(final IEventBus modEventBus) {
        GravityGunRegistry.register(modEventBus);
        LOGGER.info("Create: Physics Gun loaded");
    }
}
