package org.eckelsoft.fullbrightdamagemeterzoomplus.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.LightmapTextureManager;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @WrapOperation(
            method = "update",
            at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F")
    )
    private float wrapGammaValue(Double instance, Operation<Float> original) {
        if (Fullbrightdamagemeterzoomplus.fullbrightEnabled) {
            return 10.0f;
        }
        return original.call(instance);
    }
}