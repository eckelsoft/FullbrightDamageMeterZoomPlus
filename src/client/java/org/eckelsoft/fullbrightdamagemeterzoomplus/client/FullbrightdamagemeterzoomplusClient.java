package org.eckelsoft.fullbrightdamagemeterzoomplus.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.eckelsoft.fullbrightdamagemeterzoomplus.Fullbrightdamagemeterzoomplus;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class FullbrightdamagemeterzoomplusClient implements ClientModInitializer {
    private static KeyBinding keyFullbright, keyZoom, keyMobHp, keyDamageMeter;
    private final Random random = new Random();
    private final Map<Integer, ArmorStandEntity> hpLabels = new HashMap<>();
    private final List<FloatingTextEntry> activeLabels = new ArrayList<>();

    public static class FloatingTextEntry {
        public final ArmorStandEntity entity;
        public int remainingTicks;

        public FloatingTextEntry(ArmorStandEntity entity, int ticks) {
            this.entity = entity;
            this.remainingTicks = ticks;
        }
    }

    @Override
    public void onInitializeClient() {
        final KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of(Fullbrightdamagemeterzoomplus.MOD_ID, "main"));
        keyFullbright = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fdmzp.fullbright", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, cat));
        keyZoom = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fdmzp.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, cat));
        keyMobHp = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fdmzp.mobhp", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, cat));
        keyDamageMeter = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.fdmzp.damagemeter", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, cat));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            hpLabels.clear();
            activeLabels.clear();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            while (keyFullbright.wasPressed()) {
                Fullbrightdamagemeterzoomplus.fullbrightEnabled = !Fullbrightdamagemeterzoomplus.fullbrightEnabled;
                client.gameRenderer.getLightmapTextureManager().tick();
            }
            if (keyZoom.isPressed()) Fullbrightdamagemeterzoomplus.isZooming = true;
            else { Fullbrightdamagemeterzoomplus.isZooming = false; Fullbrightdamagemeterzoomplus.zoomLevel = 0.25f; }

            while (keyMobHp.wasPressed()) {
                Fullbrightdamagemeterzoomplus.mobHpMode = (Fullbrightdamagemeterzoomplus.mobHpMode + 1) % 4;
                hpLabels.values().forEach(Entity::discard);
                hpLabels.clear();
            }
            while (keyDamageMeter.wasPressed()) {
                Fullbrightdamagemeterzoomplus.damageMeterEnabled = !Fullbrightdamagemeterzoomplus.damageMeterEnabled;
            }

            Iterator<FloatingTextEntry> iterator = activeLabels.iterator();
            while (iterator.hasNext()) {
                FloatingTextEntry entry = iterator.next();
                if (entry.remainingTicks > 0) {
                    entry.entity.setPosition(entry.entity.getX(), entry.entity.getY() + 0.02, entry.entity.getZ());
                    entry.remainingTicks--;
                } else {
                    entry.entity.discard();
                    iterator.remove();
                }
            }

            boolean showLabels = (Fullbrightdamagemeterzoomplus.mobHpMode == 2 || Fullbrightdamagemeterzoomplus.mobHpMode == 3);
            if (showLabels) {
                List<LivingEntity> entities = client.world.getEntitiesByClass(LivingEntity.class, client.player.getBoundingBox().expand(20.0),
                        e -> e != client.player && e.isAlive() && !e.isInvisible() && !(e instanceof ArmorStandEntity));
                for (LivingEntity entity : entities) {
                    ArmorStandEntity label = hpLabels.computeIfAbsent(entity.getId(), id -> {
                        ArmorStandEntity as = new ArmorStandEntity(EntityType.ARMOR_STAND, client.world);
                        as.setInvisible(true);
                        as.setNoGravity(true);
                        as.setCustomNameVisible(true);
                        setArmorStandMarker(as);
                        client.world.addEntity(as);
                        return as;
                    });
                    label.setPosition(entity.getX(), entity.getY() + entity.getHeight() + 0.3, entity.getZ());
                    label.setCustomName(Text.literal(String.format("§a%.1f HP", entity.getHealth())));
                }
            }
            hpLabels.entrySet().removeIf(entry -> {
                Entity e = client.world.getEntityById(entry.getKey());
                if (!showLabels || !(e instanceof LivingEntity le) || !le.isAlive() || le instanceof ArmorStandEntity || le.squaredDistanceTo(client.player) > 400.0) {
                    entry.getValue().discard();
                    return true;
                }
                return false;
            });
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && player.isMainPlayer() && entity instanceof LivingEntity target && !(target instanceof ArmorStandEntity)) {
                if (Fullbrightdamagemeterzoomplus.damageMeterEnabled) {
                    double damage = player.getAttributeBaseValue(EntityAttributes.ATTACK_DAMAGE);
                    AttributeModifiersComponent modComponent = player.getMainHandStack().get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                    if (modComponent != null) {
                        for (AttributeModifiersComponent.Entry entry : modComponent.modifiers()) {
                            if (entry.attribute().value().equals(EntityAttributes.ATTACK_DAMAGE.value())) damage += entry.modifier().value();
                        }
                    }
                    if (player.hasStatusEffect(StatusEffects.STRENGTH)) damage += (player.getStatusEffect(StatusEffects.STRENGTH).getAmplifier() + 1) * 3;
                    if (player.hasStatusEffect(StatusEffects.WEAKNESS)) damage -= (player.getStatusEffect(StatusEffects.WEAKNESS).getAmplifier() + 1) * 4;
                    float cooldown = player.getAttackCooldownProgress(0.5f);
                    double finalDamage = Math.max(0, damage * (0.2f + cooldown * cooldown * 0.8f));
                    boolean isCrit = player.fallDistance > 0.0f && !player.isOnGround() && !player.isClimbing() && !player.isTouchingWater();
                    if (isCrit) finalDamage *= 1.5;
                    spawnDamageLabel(target, (ClientWorld) world, finalDamage, isCrit);
                }
            }
            return ActionResult.PASS;
        });
    }

    private void setArmorStandMarker(ArmorStandEntity as) {
        byte b = as.getDataTracker().get(ArmorStandEntity.ARMOR_STAND_FLAGS);
        as.getDataTracker().set(ArmorStandEntity.ARMOR_STAND_FLAGS, (byte)(b | 0x10));
    }

    private void spawnDamageLabel(LivingEntity entity, ClientWorld world, double damage, boolean isCrit) {
        ArmorStandEntity floatingText = new ArmorStandEntity(EntityType.ARMOR_STAND, world);
        floatingText.refreshPositionAndAngles(entity.getX() + (random.nextDouble() * 0.6 - 0.3), entity.getY() + entity.getHeight() + 0.2, entity.getZ() + (random.nextDouble() * 0.6 - 0.3), 0, 0);
        floatingText.setInvisible(true);
        floatingText.setNoGravity(true);
        floatingText.setCustomNameVisible(true);
        setArmorStandMarker(floatingText);
        String color = isCrit ? "§c§l" : "§e";
        floatingText.setCustomName(Text.literal(color + String.format("%.1f HP", damage)));
        world.addEntity(floatingText);
        activeLabels.add(new FloatingTextEntry(floatingText, 40));
    }
}