package org.eckelsoft.fullbrightdamagemeterzoomplus.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MouseHandler;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {
    private static final float MAX_ZOOM_LEVEL = 1.0F;
    private static final float MIN_ZOOM_LEVEL = 0.0001F;
    private static final float ZOOM_IN_FACTOR = 0.7F;
    private static final float ZOOM_OUT_FACTOR = 1.3F;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!Fullbrightdamagemeterzoomplus.isZooming) {
            return;
        }

        if (vertical > 0.0D) {
            Fullbrightdamagemeterzoomplus.zoomLevel *= ZOOM_IN_FACTOR;
        } else if (vertical < 0.0D) {
            Fullbrightdamagemeterzoomplus.zoomLevel *= ZOOM_OUT_FACTOR;
        }

        if (Fullbrightdamagemeterzoomplus.zoomLevel > MAX_ZOOM_LEVEL) {
            Fullbrightdamagemeterzoomplus.zoomLevel = MAX_ZOOM_LEVEL;
        }
        if (Fullbrightdamagemeterzoomplus.zoomLevel < MIN_ZOOM_LEVEL) {
            Fullbrightdamagemeterzoomplus.zoomLevel = MIN_ZOOM_LEVEL;
        }

        ci.cancel();
    }

    @ModifyArgs(
            method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")
    )
    private void scaleSensitivity(Args args) {
        if (!Fullbrightdamagemeterzoomplus.isZooming) {
            return;
        }

        double deltaX = args.get(0);
        double deltaY = args.get(1);
        double scale = Fullbrightdamagemeterzoomplus.zoomLevel;
        args.set(0, deltaX * scale);
        args.set(1, deltaY * scale);
    }
}
