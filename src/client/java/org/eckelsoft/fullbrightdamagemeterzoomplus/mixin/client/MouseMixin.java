package org.eckelsoft.fullbrightdamagemeterzoomplus.mixin.client;

import net.minecraft.client.Mouse;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (Fullbrightdamagemeterzoomplus.isZooming) {
            if (vertical > 0) {
                Fullbrightdamagemeterzoomplus.zoomLevel *= 0.9f;
            } else if (vertical < 0) {
                Fullbrightdamagemeterzoomplus.zoomLevel *= 1.1f;
            }

            if (Fullbrightdamagemeterzoomplus.zoomLevel > 1.0f) Fullbrightdamagemeterzoomplus.zoomLevel = 1.0f;
            if (Fullbrightdamagemeterzoomplus.zoomLevel < 0.001f) Fullbrightdamagemeterzoomplus.zoomLevel = 0.001f;

            ci.cancel();
        }
    }

    @ModifyArgs(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private void scaleSensitivity(Args args) {
        if (Fullbrightdamagemeterzoomplus.isZooming) {
            double deltaX = args.get(0);
            double deltaY = args.get(1);

            args.set(0, deltaX * (double) Fullbrightdamagemeterzoomplus.zoomLevel);
            args.set(1, deltaY * (double) Fullbrightdamagemeterzoomplus.zoomLevel);
        }
    }
}