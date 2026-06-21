package org.eckelsoft.fullbrightdamagemeterzoomplus.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FullbrightdamagemeterzoomplusClient implements ClientModInitializer {
    private static final int DAMAGE_LABEL_LIFETIME = 40;
    private static final Identifier KEY_CATEGORY_ID = Identifier.fromNamespaceAndPath(Fullbrightdamagemeterzoomplus.MOD_ID, "main");
    private static final Identifier HUD_OVERLAY_ID = Identifier.fromNamespaceAndPath(Fullbrightdamagemeterzoomplus.MOD_ID, "overlay");

    private static KeyMapping keyFullbright;
    private static KeyMapping keyZoom;
    private static KeyMapping keyMobHp;
    private static KeyMapping keyDamageMeter;

    private final List<DamageLabel> damageLabels = new ArrayList<>();

    private record DamageLabel(String text, boolean critical, int remainingTicks) {
        private DamageLabel tick() {
            return new DamageLabel(text, critical, remainingTicks - 1);
        }
    }

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(KEY_CATEGORY_ID);
        keyFullbright = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fullbrightdamagemeterzoomplus.fullbright",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                category
        ));
        keyZoom = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fullbrightdamagemeterzoomplus.zoom",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                category
        ));
        keyMobHp = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fullbrightdamagemeterzoomplus.mobhp",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                category
        ));
        keyDamageMeter = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fullbrightdamagemeterzoomplus.damagemeter",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                category
        ));

        HudElementRegistry.addLast(HUD_OVERLAY_ID, this::renderOverlay);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> resetClientState());
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            Minecraft client = Minecraft.getInstance();
            if (!Fullbrightdamagemeterzoomplus.damageMeterEnabled) {
                return InteractionResult.PASS;
            }

            if (client.player == null || client.level == null || !world.isClientSide() || player != client.player) {
                return InteractionResult.PASS;
            }

            if (!(entity instanceof LivingEntity living) || entity instanceof ArmorStand) {
                return InteractionResult.PASS;
            }

            double damage = estimateDamage(player);
            boolean critical = player.fallDistance > 0.0D
                    && !player.onGround()
                    && !player.onClimbable()
                    && !player.isInWater()
                    && !player.isUnderWater();
            if (critical) {
                damage *= 1.5D;
            }

            this.damageLabels.add(new DamageLabel(
                    String.format(Locale.ROOT, "%.1f", Math.max(0.0D, damage)),
                    critical,
                    DAMAGE_LABEL_LIFETIME
            ));

            return InteractionResult.PASS;
        });
    }

    private void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        while (keyFullbright.consumeClick()) {
            toggleFullbright();
        }

        Fullbrightdamagemeterzoomplus.isZooming = keyZoom.isDown();
        if (!Fullbrightdamagemeterzoomplus.isZooming) {
            Fullbrightdamagemeterzoomplus.resetZoom();
        }

        while (keyMobHp.consumeClick()) {
            Fullbrightdamagemeterzoomplus.mobHpMode = (Fullbrightdamagemeterzoomplus.mobHpMode + 1) % 4;
        }

        while (keyDamageMeter.consumeClick()) {
            Fullbrightdamagemeterzoomplus.damageMeterEnabled = !Fullbrightdamagemeterzoomplus.damageMeterEnabled;
        }

        tickDamageLabels();
    }

    private void renderOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }

        int width = client.getWindow().getGuiScaledWidth();
        int height = client.getWindow().getGuiScaledHeight();

        if (Fullbrightdamagemeterzoomplus.mobHpMode == 1 || Fullbrightdamagemeterzoomplus.mobHpMode == 3) {
            renderTargetHealthBar(graphics, client, width);
        }

        if (Fullbrightdamagemeterzoomplus.mobHpMode == 2 || Fullbrightdamagemeterzoomplus.mobHpMode == 3) {
            renderNearbyEntityList(graphics, client);
        }

        renderDamageLabels(graphics, client, width, height);
    }

    private void renderTargetHealthBar(GuiGraphicsExtractor graphics, Minecraft client, int width) {
        Entity targeted = client.crosshairPickEntity;
        if (!(targeted instanceof LivingEntity living) || !living.isAlive() || targeted instanceof ArmorStand) {
            return;
        }

        float health = living.getHealth();
        float maxHealth = Math.max(1.0F, living.getMaxHealth());
        float healthPercent = Mth.clamp(health / maxHealth, 0.0F, 1.0F);

        int barWidth = 120;
        int barHeight = 6;
        int x = (width - barWidth) / 2;
        int y = 20;

        graphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xAA222222);
        graphics.fill(x, y, x + Math.round(barWidth * healthPercent), y + barHeight, healthColor(healthPercent));

        String label = String.format(Locale.ROOT, "%s %.1f/%.1f HP", living.getDisplayName().getString(), health, maxHealth);
        graphics.centeredText(client.font, Component.literal(label), width / 2, y - 10, 0xFFFFFFFF);
    }

    private void renderNearbyEntityList(GuiGraphicsExtractor graphics, Minecraft client) {
        List<Entity> entities = client.level.getEntities(
                client.player,
                client.player.getBoundingBox().inflate(16.0D),
                entity -> entity instanceof LivingEntity living && living.isAlive() && entity != client.player && !(entity instanceof ArmorStand)
        );

        entities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(client.player)));

        int x = 8;
        int y = 8;
        graphics.text(client.font, Component.literal("Nearby mobs"), x, y, 0xFFFFFFFF);
        y += 12;

        for (Entity entity : entities.stream().limit(5).toList()) {
            LivingEntity living = (LivingEntity) entity;
            float health = living.getHealth();
            float maxHealth = Math.max(1.0F, living.getMaxHealth());
            float healthPercent = Mth.clamp(health / maxHealth, 0.0F, 1.0F);
            int color = healthColor(healthPercent);
            String line = String.format(Locale.ROOT, "%s %.1f/%.1f", living.getDisplayName().getString(), health, maxHealth);
            graphics.text(client.font, Component.literal(line), x, y, color);
            y += 10;
        }
    }

    private void renderDamageLabels(GuiGraphicsExtractor graphics, Minecraft client, int width, int height) {
        if (damageLabels.isEmpty()) {
            return;
        }

        int y = height / 2 - 40;
        int index = 0;
        for (DamageLabel label : damageLabels) {
            int alpha = (int) Math.max(64, 255.0D * (label.remainingTicks / (double) DAMAGE_LABEL_LIFETIME));
            int color = label.critical ? (alpha << 24) | 0xFF6666 : (alpha << 24) | 0xFFD8A100;
            int offsetY = y - (index * 12) - (DAMAGE_LABEL_LIFETIME - label.remainingTicks) / 2;
            String text = label.critical ? label.text + "!" : label.text;
            graphics.centeredText(client.font, Component.literal(text), width / 2, offsetY, color);
            index++;
        }
    }

    private void tickDamageLabels() {
        Iterator<DamageLabel> iterator = damageLabels.iterator();
        List<DamageLabel> updated = new ArrayList<>(damageLabels.size());
        while (iterator.hasNext()) {
            DamageLabel label = iterator.next().tick();
            if (label.remainingTicks > 0) {
                updated.add(label);
            }
        }
        damageLabels.clear();
        damageLabels.addAll(updated);
    }

    private double estimateDamage(net.minecraft.world.entity.player.Player player) {
        double damage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        MobEffectInstance strength = player.getEffect(MobEffects.STRENGTH);
        if (strength != null) {
            damage += 3.0D * (strength.getAmplifier() + 1);
        }

        MobEffectInstance weakness = player.getEffect(MobEffects.WEAKNESS);
        if (weakness != null) {
            damage -= 4.0D * (weakness.getAmplifier() + 1);
        }

        float cooldown = player.getAttackStrengthScale(0.5F);
        damage *= 0.2D + cooldown * cooldown * 0.8D;
        return Math.max(0.0D, damage);
    }

    private void toggleFullbright() {
        Fullbrightdamagemeterzoomplus.fullbrightEnabled = !Fullbrightdamagemeterzoomplus.fullbrightEnabled;
    }

    private void resetClientState() {
        Fullbrightdamagemeterzoomplus.isZooming = false;
        Fullbrightdamagemeterzoomplus.resetZoom();
        Fullbrightdamagemeterzoomplus.mobHpMode = 0;
        Fullbrightdamagemeterzoomplus.damageMeterEnabled = false;
        Fullbrightdamagemeterzoomplus.fullbrightEnabled = false;
        tickDamageLabels();
    }

    private static int healthColor(float healthPercent) {
        float clamped = Mth.clamp(healthPercent, 0.0F, 1.0F);
        int red = (int) ((1.0F - clamped) * 255.0F);
        int green = (int) (clamped * 255.0F);
        return 0xFF000000 | (red << 16) | (green << 8);
    }
}
