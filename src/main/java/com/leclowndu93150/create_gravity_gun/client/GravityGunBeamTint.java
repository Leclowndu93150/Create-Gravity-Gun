package com.leclowndu93150.create_gravity_gun.client;

public final class GravityGunBeamTint {
    private static final ThreadLocal<Boolean> TINTING = ThreadLocal.withInitial(() -> false);

    private GravityGunBeamTint() {}

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
