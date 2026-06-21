package org.eckelsoft.fullbrightdamagemeterzoomplus.mixin.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void hidePlayerInThirdPersonZoom(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (Fullbrightdamagemeterzoomplus.isZooming
                && client.player != null
                && entity == client.player
                && client.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            cir.setReturnValue(false);
        }
    }
}
