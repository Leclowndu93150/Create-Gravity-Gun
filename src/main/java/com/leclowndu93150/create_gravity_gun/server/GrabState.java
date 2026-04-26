package com.leclowndu93150.create_gravity_gun.server;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public final class GrabState {
    @Nullable public ServerSubLevel grabbedSubLevel;
    @Nullable public Entity grabbedEntity;
    public final Vector3d subLevelLocalAnchor = new Vector3d();
    public double holdDistance = 4.0;
    public boolean freshGrab = false;

    public boolean isHolding() {
        return this.grabbedSubLevel != null || this.grabbedEntity != null;
    }

    public void clear() {
        this.grabbedSubLevel = null;
        this.grabbedEntity = null;
        this.freshGrab = false;
    }
}
