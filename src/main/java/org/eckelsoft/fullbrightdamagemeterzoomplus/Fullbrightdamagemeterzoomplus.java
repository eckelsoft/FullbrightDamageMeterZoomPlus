package org.eckelsoft.fullbrightdamagemeterzoomplus;

import net.fabricmc.api.ModInitializer;

public class Fullbrightdamagemeterzoomplus implements ModInitializer {
    public static final String MOD_ID = "fullbrightdamagemeterzoomplus";
    public static boolean fullbrightEnabled = false;
    public static int mobHpMode = 0; // 0=AUS, 1=MITTE, 2=OBEN
    public static boolean damageMeterEnabled = false;
    public static boolean isZooming = false;
    public static float zoomLevel = 0.25f;
    public static long lastDebugTime = 0;

    @Override
    public void onInitialize() {}
}