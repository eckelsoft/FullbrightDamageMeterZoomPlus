package org.eckelsoft.fullbrightdamagemeterzoomplus.mixin.client;

import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.world.level.dimension.DimensionType;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapRenderStateExtractorMixin {
    @Inject(method = "calculateDarknessScale", at = @At("RETURN"), cancellable = true)
    private void forceNoDarkness(net.minecraft.world.entity.LivingEntity entity, float tickDelta, float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (Fullbrightdamagemeterzoomplus.fullbrightEnabled) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "extract", at = @At("TAIL"))
    private void forceBrightState(LightmapRenderState state, float tickDelta, CallbackInfo ci) {
        if (!Fullbrightdamagemeterzoomplus.fullbrightEnabled) {
            return;
        }

        state.blockFactor = 1.0F;
        state.skyFactor = 1.0F;
        state.brightness = 1.0F;
        state.darknessEffectScale = 0.0F;
        state.nightVisionEffectIntensity = 1.0F;
        state.bossOverlayWorldDarkening = 0.0F;
        state.blockLightTint = LightmapRenderStateExtractor.WHITE;
        state.skyLightColor = LightmapRenderStateExtractor.WHITE;
        state.ambientColor = LightmapRenderStateExtractor.WHITE;
        state.nightVisionColor = LightmapRenderStateExtractor.WHITE;
        state.needsUpdate = true;
    }
}
