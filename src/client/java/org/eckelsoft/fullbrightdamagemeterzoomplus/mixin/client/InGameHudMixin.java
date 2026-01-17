package org.eckelsoft.fullbrightdamagemeterzoomplus.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderMobHealthBar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (Fullbrightdamagemeterzoomplus.mobHpMode == 1 || Fullbrightdamagemeterzoomplus.mobHpMode == 3) {
            if (client.targetedEntity instanceof LivingEntity target && target.isAlive() && !(target instanceof ArmorStandEntity)) {
                int width = context.getScaledWindowWidth();
                int barWidth = 100;
                int x = (width - barWidth) / 2;
                int y = 20;

                float healthPercent = Math.max(0, target.getHealth() / target.getMaxHealth());

                context.fill(x, y, x + barWidth, y + 5, 0x80000000);
                context.fill(x, y, x + (int)(barWidth * healthPercent), y + 5, 0xFFFF0000);

                String info = target.getType().getName().getString() + " " + String.format("%.1f/%.1f", target.getHealth(), target.getMaxHealth());
                context.drawCenteredTextWithShadow(client.textRenderer, Text.literal(info), width / 2, y - 10, 0xFFFFFFFF);
            }
        }
    }
}