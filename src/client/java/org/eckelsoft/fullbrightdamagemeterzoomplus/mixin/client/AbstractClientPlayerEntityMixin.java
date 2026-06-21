package org.eckelsoft.fullbrightdamagemeterzoomplus.mixin.client;

import net.minecraft.client.player.AbstractClientPlayer;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void injectZoomMultiplier(boolean changingFov, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (Fullbrightdamagemeterzoomplus.isZooming) {
            cir.setReturnValue(cir.getReturnValue() * Fullbrightdamagemeterzoomplus.zoomLevel);
        }
    }
}
