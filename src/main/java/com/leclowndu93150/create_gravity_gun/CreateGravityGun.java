package com.leclowndu93150.create_gravity_gun;

import com.leclowndu93150.create_gravity_gun.config.GravityGunConfig;
import com.leclowndu93150.create_gravity_gun.registry.GravityGunComponents;
import com.leclowndu93150.create_gravity_gun.registry.GravityGunRegistry;
import com.leclowndu93150.create_gravity_gun.registry.GravityGunSounds;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CreateGravityGun.MODID)
public class CreateGravityGun {
    public static final String MODID = "create_gravity_gun";
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateGravityGun(final IEventBus modEventBus, final ModContainer container) {
        GravityGunRegistry.register(modEventBus);
        GravityGunSounds.register(modEventBus);
        GravityGunComponents.register(modEventBus);
        GravityGunConfig.register(container);
        LOGGER.info("Create: Gravity Gun loaded");
    }
}
