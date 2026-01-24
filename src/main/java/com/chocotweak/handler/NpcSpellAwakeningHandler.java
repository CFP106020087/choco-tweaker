package com.chocotweak.handler;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import com.chocolate.chocolateQuest.items.ItemStaffBase;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.magic.SpellBase;
import com.chocotweak.ChocoTweak;
import com.chocotweak.core.AwakementsInitializer;
import com.chocotweak.magic.AwakementEchoingVoice;
import com.chocotweak.magic.AwakementInstantCast;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * NPC 法术觉醒效果事件处理器
 * 
 * 直接通过调用法术来实现觉醒效果：
 * - 瞬发咏唱: 减少施法间隔
 * - 高速神言: 连续快速施法
 * - 回响之音: 概率免费重放
 */
public class NpcSpellAwakeningHandler {

    // 追踪 NPC 上次施法时间
    private static final Map<Integer, Long> lastCastTime = new HashMap<>();
    // 追踪 NPC 上次回响时间
    private static final Map<Integer, Long> lastEchoTime = new HashMap<>();

    // 反射缓存
    private static Field cachedTrackerField = null;
    private static Field castingSpellField = null;
    private static Field cooldownsField = null;
    private static Field spellsField = null;
    private static Method getElementMethod = null;
    private static boolean reflectionInitialized = false;

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();

        // 只处理 CQ NPC
        if (!(entity instanceof EntityHumanBase)) {
            return;
        }

        // 只在服务端处理
        if (entity.world.isRemote) {
            return;
        }

        EntityHumanBase npc = (EntityHumanBase) entity;
        ItemStack mainHand = npc.getHeldItemMainhand();

        // 检查是否持有法杖
        if (mainHand.isEmpty() || !(mainHand.getItem() instanceof ItemStaffBase)) {
            return;
        }

        // 检查是否有攻击目标
        EntityLivingBase target = npc.getAttackTarget();
        if (target == null || !target.isEntityAlive()) {
            return;
        }

        ItemStaffBase staff = (ItemStaffBase) mainHand.getItem();

        // 确保反射已初始化
        if (!reflectionInitialized) {
            initReflection(staff);
        }

        // 获取觉醒等级
        int instantCastLevel = 0;
        int highSpeedLevel = 0;
        int echoLevel = 0;

        if (AwakementsInitializer.instantCast != null) {
            instantCastLevel = Awakements.getEnchantLevel(mainHand, (Awakements) AwakementsInitializer.instantCast);
        }
        if (AwakementsInitializer.highSpeedChant != null) {
            highSpeedLevel = Awakements.getEnchantLevel(mainHand, (Awakements) AwakementsInitializer.highSpeedChant);
        }
        if (AwakementsInitializer.echoingVoice != null) {
            echoLevel = Awakements.getEnchantLevel(mainHand, (Awakements) AwakementsInitializer.echoingVoice);
        }

        // 没有任何觉醒则跳过
        if (instantCastLevel <= 0 && highSpeedLevel <= 0 && echoLevel <= 0) {
            return;
        }

        // 计算施法间隔（基础 40 ticks = 2秒）
        int baseCastInterval = 40;

        // 瞬发咏唱减少间隔
        if (instantCastLevel > 0) {
            float multiplier = AwakementInstantCast.getCooldownMultiplier(instantCastLevel);
            baseCastInterval = (int) (baseCastInterval * multiplier);
        }

        // 高速神言大幅减少间隔
        if (highSpeedLevel > 0) {
            float reduction = 1.0f - (highSpeedLevel * 0.15f); // 每级减少15%
            reduction = Math.max(0.25f, reduction); // 最多减少75%
            baseCastInterval = (int) (baseCastInterval * reduction);
        }

        // 最小间隔 5 ticks
        baseCastInterval = Math.max(5, baseCastInterval);

        long currentTime = npc.world.getTotalWorldTime();
        long lastCast = lastCastTime.getOrDefault(npc.getEntityId(), 0L);

        // 检查是否可以施法
        if (currentTime - lastCast >= baseCastInterval) {
            // 尝试触发法术
            if (tryForceSpellCast(npc, staff, mainHand, target)) {
                lastCastTime.put(npc.getEntityId(), currentTime);
                npc.swingArm(EnumHand.MAIN_HAND);

                // 回响之音：概率立即再放一次
                if (echoLevel > 0 && AwakementEchoingVoice.shouldEcho(echoLevel)) {
                    long lastEcho = lastEchoTime.getOrDefault(npc.getEntityId(), 0L);
                    if (currentTime - lastEcho > 10) { // 防止连续触发
                        lastEchoTime.put(npc.getEntityId(), currentTime);
                        tryForceSpellCast(npc, staff, mainHand, target);
                        npc.swingArm(EnumHand.MAIN_HAND);
                        ChocoTweak.LOGGER.debug("[EchoingVoice] NPC {} triggered echo!", npc.getName());
                    }
                }
            }
        }
    }

    /**
     * 强制触发法术施放
     */
    private boolean tryForceSpellCast(EntityHumanBase npc, ItemStaffBase staff, ItemStack staffItem,
            EntityLivingBase target) {
        try {
            if (cachedTrackerField == null || spellsField == null || cooldownsField == null) {
                ChocoTweak.LOGGER.warn("[NpcAwakening] Reflection not ready");
                return false;
            }

            // 获取 CoolDownTracker
            Object tracker = cachedTrackerField.get(staff);
            if (tracker == null) {
                ChocoTweak.LOGGER.warn("[NpcAwakening] CachedTracker is null");
                return false;
            }

            // 获取法术数组和冷却数组
            SpellBase[] spells = (SpellBase[]) spellsField.get(tracker);
            int[] cooldowns = (int[]) cooldownsField.get(tracker);

            if (spells == null || cooldowns == null) {
                ChocoTweak.LOGGER.warn("[NpcAwakening] Spells or cooldowns array is null");
                return false;
            }

            // 找到第一个可用的法术
            for (int i = 0; i < spells.length; i++) {
                SpellBase spell = spells[i];
                if (spell != null) {
                    // 检查距离
                    double distSq = npc.getDistanceSq(target);
                    float range = spell.getRange(staffItem);

                    if (distSq < range * range) {
                        // 获取元素
                        com.chocolate.chocolateQuest.magic.Elements element = null;
                        if (getElementMethod != null) {
                            element = (com.chocolate.chocolateQuest.magic.Elements) getElementMethod.invoke(staff,
                                    staffItem);
                        }

                        // 让 NPC 面向目标以确保法术瞄准正确
                        faceTarget(npc, target);

                        // 直接触发法术
                        spell.onCastStart(npc, element, staffItem);
                        spell.onShoot(npc, element, staffItem, 5);

                        // 重置冷却（设为较短值让原版 AI 也能正常工作）
                        cooldowns[i] = 10;

                        ChocoTweak.LOGGER.debug("[NpcAwakening] Forced spell cast: {}",
                                spell.getClass().getSimpleName());
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            ChocoTweak.LOGGER.error("[NpcAwakening] Error forcing spell cast: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 初始化反射
     */
    private void initReflection(ItemStaffBase staff) {
        reflectionInitialized = true;

        try {
            Class<?> staffClass = staff.getClass();

            // 找 cachedTracker 字段
            try {
                cachedTrackerField = staffClass.getDeclaredField("cachedTracker");
                cachedTrackerField.setAccessible(true);
                ChocoTweak.LOGGER.info("[NpcAwakening] Found cachedTracker field in {}", staffClass.getName());
            } catch (NoSuchFieldException e) {
                // 尝试父类
                cachedTrackerField = ItemStaffBase.class.getDeclaredField("cachedTracker");
                cachedTrackerField.setAccessible(true);
                ChocoTweak.LOGGER.info("[NpcAwakening] Found cachedTracker in ItemStaffBase");
            }

            // 找 getElement 方法
            try {
                getElementMethod = ItemStaffBase.class.getDeclaredMethod("getElement", ItemStack.class);
                getElementMethod.setAccessible(true);
                ChocoTweak.LOGGER.info("[NpcAwakening] Found getElement method");
            } catch (NoSuchMethodException e) {
                ChocoTweak.LOGGER.warn("[NpcAwakening] getElement method not found");
            }

            // 需要先获取一个 tracker 实例来找字段
            if (cachedTrackerField != null) {
                Object tracker = cachedTrackerField.get(staff);
                if (tracker != null) {
                    Class<?> trackerClass = tracker.getClass();
                    ChocoTweak.LOGGER.info("[NpcAwakening] CoolDownTracker class: {}", trackerClass.getName());

                    spellsField = trackerClass.getDeclaredField("spells");
                    spellsField.setAccessible(true);

                    cooldownsField = trackerClass.getDeclaredField("cooldowns");
                    cooldownsField.setAccessible(true);

                    castingSpellField = trackerClass.getDeclaredField("castingSpell");
                    castingSpellField.setAccessible(true);

                    ChocoTweak.LOGGER.info("[NpcAwakening] Reflection initialized successfully");
                } else {
                    ChocoTweak.LOGGER.warn("[NpcAwakening] cachedTracker is null during init");
                }
            }
        } catch (Exception e) {
            ChocoTweak.LOGGER.error("[NpcAwakening] Failed to initialize reflection: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 让 NPC 面向目标，确保法术瞄准正确
     * 复制 CQ 的 NPC 瞄准行为
     */
    private void faceTarget(EntityHumanBase npc, EntityLivingBase target) {
        // 计算目标方向
        double dx = target.posX - npc.posX;
        double dy = (target.posY + target.getEyeHeight()) - (npc.posY + npc.getEyeHeight());
        double dz = target.posZ - npc.posZ;

        double distXZ = Math.sqrt(dx * dx + dz * dz);

        // 计算 yaw (水平旋转)
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        // 计算 pitch (垂直旋转)
        float pitch = (float) -(Math.atan2(dy, distXZ) * 180.0 / Math.PI);

        // 直接设置 NPC 的朝向
        npc.rotationYaw = yaw;
        npc.rotationYawHead = yaw;
        npc.rotationPitch = pitch;

        // 同步身体朝向
        npc.renderYawOffset = yaw;
    }
}
