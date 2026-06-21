package org.eckelsoft.fullbrightdamagemeterzoomplus.mixin.client;

import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {
    private static final float MIN_MOUSE_SENSITIVITY = 0.25F;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!Fullbrightdamagemeterzoomplus.isZooming) {
            return;
        }

        if (vertical > 0.0D) {
            Fullbrightdamagemeterzoomplus.zoomIn((float) vertical);
        } else if (vertical < 0.0D) {
            Fullbrightdamagemeterzoomplus.zoomOut((float) -vertical);
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
        double scale = getMouseSensitivityScale();
        args.set(0, deltaX * scale);
        args.set(1, deltaY * scale);
    }

    private static float getMouseSensitivityScale() {
        float zoomPower = Fullbrightdamagemeterzoomplus.zoomPower;
        float softenedScale = (float) Math.sqrt(1.0F / zoomPower);
        return Mth.clamp(softenedScale, MIN_MOUSE_SENSITIVITY, 1.0F);
    }
}
