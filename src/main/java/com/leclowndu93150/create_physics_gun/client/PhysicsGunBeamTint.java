package com.leclowndu93150.create_physics_gun.client;

public final class PhysicsGunBeamTint {
    private static final ThreadLocal<Boolean> TINTING = ThreadLocal.withInitial(() -> false);

    private PhysicsGunBeamTint() {}

    public static boolean tinting() {
        return TINTING.get();
    }

    public static void run(final Runnable runnable) {
        TINTING.set(true);
        try {
            runnable.run();
        } finally {
            TINTING.set(false);
        }
    }
}
