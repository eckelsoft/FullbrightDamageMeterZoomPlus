package org.eckelsoft.fullbrightdamagemeterzoomplus.mixin.client;

import net.minecraft.client.renderer.Lightmap;
import net.minecraft.world.level.dimension.DimensionType;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Lightmap.class)
public class LightmapMixin {
    @Inject(method = "getBrightness", at = @At("RETURN"), cancellable = true)
    private static void forceMaxBrightness(DimensionType dimensionType, int lightLevel, CallbackInfoReturnable<Float> cir) {
        if (Fullbrightdamagemeterzoomplus.fullbrightEnabled) {
            cir.setReturnValue(1.0F);
        }
    }
}
