package org.eckelsoft.fullbrightdamagemeterzoomplus;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Mth;

public class Fullbrightdamagemeterzoomplus implements ModInitializer {
    public static final String MOD_ID = "fullbrightdamagemeterzoomplus";
    public static final float DEFAULT_ZOOM_POWER = 4.0f;
    public static final float MIN_ZOOM_POWER = 1.0f;
    public static final float MAX_ZOOM_POWER = 100.0f;
    public static final float ZOOM_IN_FACTOR = 1.35f;
    public static final float ZOOM_OUT_FACTOR = 1.35f;

    public static boolean fullbrightEnabled = false;
    public static int mobHpMode = 0; // 0=AUS, 1=MITTE, 2=OBEN
    public static boolean damageMeterEnabled = false;
    public static boolean isZooming = false;
    public static float zoomPower = DEFAULT_ZOOM_POWER;
    public static long lastDebugTime = 0;

    @Override
    public void onInitialize() {}

    public static void resetZoom() {
        zoomPower = DEFAULT_ZOOM_POWER;
    }

    public static void zoomIn(float scrollAmount) {
        zoomPower *= (float) Math.pow(ZOOM_IN_FACTOR, scrollAmount);
        clampZoom();
    }

    public static void zoomOut(float scrollAmount) {
        zoomPower /= (float) Math.pow(ZOOM_OUT_FACTOR, scrollAmount);
        clampZoom();
    }

    public static float getCameraZoomMultiplier() {
        return Mth.clamp(1.0F / zoomPower, 0.01F, 1.0F);
    }

    private static void clampZoom() {
        if (zoomPower < MIN_ZOOM_POWER) {
            zoomPower = MIN_ZOOM_POWER;
        }
        if (zoomPower > MAX_ZOOM_POWER) {
            zoomPower = MAX_ZOOM_POWER;
        }
    }
}
