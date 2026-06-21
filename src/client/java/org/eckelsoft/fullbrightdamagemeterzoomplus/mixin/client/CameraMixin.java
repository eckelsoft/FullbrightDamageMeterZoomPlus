package org.eckelsoft.fullbrightdamagemeterzoomplus.mixin.client;

import net.minecraft.client.Camera;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void applyLensZoom(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (!Fullbrightdamagemeterzoomplus.isZooming) {
            return;
        }

        float baseFov = cir.getReturnValue();
        cir.setReturnValue(baseFov * Fullbrightdamagemeterzoomplus.getCameraZoomMultiplier());
    }
}
