package com.chocotweak.weapon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.Random;

/**
 * 武器特效事件处理器
 * 处理所有CQ剑盾的特殊效果
 * 注意：使用注册名检查代替直接类引用，避免触发ChocolateQuest类加载失败
 */
public class WeaponEventHandler {

    private static final Random random = new Random();

    // 物品注册名常量
    private static final String IRON_SWORD_SHIELD = "chocolatequest:ironswordandshield";
    private static final String DIAMOND_SWORD_SHIELD = "chocolatequest:diamondswordandshield";
    private static final String RUSTED_SWORD_SHIELD = "chocolatequest:rustedswordandshied";
    private static final String SWORD_TURTLE = "chocolatequest:swordturtle";
    private static final String SWORD_SPIDER = "chocolatequest:swordspider";
    private static final String SWORD_SUNLIGHT = "chocolatequest:swordsunlight";
    private static final String SWORD_MOONLIGHT = "chocolatequest:moonsword";
    private static final String END_SWORD = "chocolatequest:endsword";
    private static final String MONKING_SWORD_SHIELD = "chocolatequest:swordshiedmonking";

    public WeaponEventHandler() {
        System.out.println("[ChocoTweak] WeaponEventHandler registered!");
    }

    /**
     * 安全检查物品注册名
     */
    private static boolean isItem(Item item, String registryName) {
        ResourceLocation loc = item.getRegistryName();
        return loc != null && loc.toString().equals(registryName);
    }

    // ========== 受伤事件 - 减伤处理 ==========
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) entity;
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.isEmpty())
            return;

        Item item = mainHand.getItem();
        float damage = event.getAmount();

        // === 铁剑盾 - 常驻减伤12% ===
        if (isItem(item, IRON_SWORD_SHIELD)) {
            event.setAmount(damage * 0.88f);
        }
        // === 钻石剑盾 - 常驻减伤18% ===
        else if (isItem(item, DIAMOND_SWORD_SHIELD)) {
            event.setAmount(damage * 0.82f);
        }
        // === 龟盾 - 格挡80%免伤，攻击后5秒30%免伤 ===
        else if (isItem(item, SWORD_TURTLE)) {
            if (player.isActiveItemStackBlocking()) {
                // 格挡时80%免伤
                event.setAmount(damage * 0.2f);
            } else if (player.getEntityData().hasKey("chocotweak_turtle_armor")) {
                long armorEndTime = player.getEntityData().getLong("chocotweak_turtle_armor");
                if (System.currentTimeMillis() < armorEndTime) {
                    // 攻击后30%免伤
                    event.setAmount(damage * 0.7f);
                }
            }
        }
        // === 行者之剑 - 30%闪避 ===
        else if (isItem(item, END_SWORD)) {
            if (random.nextFloat() < 0.30f) {
                event.setCanceled(true);
                // 闪避粒子效果
                if (!player.world.isRemote) {
                    player.world.setEntityState(player, (byte) 2); // 伤害粒子但无伤害
                }
            }
        }
    }

    // ========== 攻击事件 - 攻击特效处理 ==========
    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) source.getTrueSource();
        EntityLivingBase target = event.getEntityLiving();
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.isEmpty())
            return;

        Item item = mainHand.getItem();
        World world = player.world;

        // === 龟盾 - 攻击后5秒免伤 ===
        if (isItem(item, SWORD_TURTLE)) {
            player.getEntityData().setLong("chocotweak_turtle_armor", System.currentTimeMillis() + 5000);
        }
        // === 锈剑盾 - 概率施加挫伤 ===
        else if (isItem(item, RUSTED_SWORD_SHIELD)) {
            if (random.nextFloat() < 0.35f) { // 35%概率
                applyWoundEffect(target);
            }
        }
        // === 阳光剑 - 白天点燃30秒 ===
        else if (isItem(item, SWORD_SUNLIGHT)) {
            if (isDaytime(world)) {
                target.setFire(30);
            }
        }
        // === 月光剑 - 夜晚混乱(恐惧) ===
        else if (isItem(item, SWORD_MOONLIGHT)) {
            if (!isDaytime(world)) {
                // 施加恐惧效果(使用缓慢+虚弱模拟)
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 2));
                target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 1));
                target.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 60, 0));
            }
        }
        // === 行者之剑 - 击中敌人debuff ===
        else if (isItem(item, END_SWORD)) {
            // 深渊侵蚀效果 - 易伤20% + 攻击+10%
            target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 0));
            // 标记为深渊腐蚀(用于易伤计算)
            target.getEntityData().setLong("chocotweak_abyss_corrosion", System.currentTimeMillis() + 5000);
        }
    }

    // ========== 伤害计算事件 - 伤害倍率处理 ==========
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingHurtDamageCalc(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getTrueSource() instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) source.getTrueSource();
        EntityLivingBase target = event.getEntityLiving();
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.isEmpty())
            return;

        Item item = mainHand.getItem();
        World world = player.world;
        float damage = event.getAmount();

        // === 锈剑盾 - 攻击力-30% ===
        if (isItem(item, RUSTED_SWORD_SHIELD)) {
            damage *= 0.7f;
        }
        // === 蜘蛛剑盾 - debuff倍率伤害 ===
        else if (isItem(item, SWORD_SPIDER)) {
            Collection<PotionEffect> effects = target.getActivePotionEffects();
            int debuffCount = 0;
            for (PotionEffect effect : effects) {
                if (effect.getPotion().isBadEffect()) {
                    debuffCount++;
                }
            }
            if (debuffCount > 0) {
                float multiplier = 1.0f + 0.2f * debuffCount;
                damage *= multiplier;
            }
        }
        // === 阳光剑 - 白天大幅增伤 ===
        else if (isItem(item, SWORD_SUNLIGHT)) {
            if (isDaytime(world)) {
                damage *= 2.5f; // 白天2.5倍伤害
            }
        }
        // === 月光剑 - 夜晚大幅增伤 + 亮度易伤 ===
        else if (isItem(item, SWORD_MOONLIGHT)) {
            if (!isDaytime(world)) {
                damage *= 2.5f; // 夜晚2.5倍伤害

                // 亮度易伤计算 (0-15亮度, 亮度越低加成越高)
                BlockPos targetPos = target.getPosition();
                int lightLevel = world.getLightFromNeighbors(targetPos);
                float darknessBonus = (15 - lightLevel) / 15.0f * 0.5f; // 最高+50%
                damage *= (1.0f + darknessBonus);
            }
        }
        // === 行者之剑 - 深渊腐蚀易伤20% ===
        else if (isItem(item, END_SWORD)) {
            if (target.getEntityData().hasKey("chocotweak_abyss_corrosion")) {
                long corrosionEndTime = target.getEntityData().getLong("chocotweak_abyss_corrosion");
                if (System.currentTimeMillis() < corrosionEndTime) {
                    damage *= 1.2f;
                }
            }
        }

        // === 燃烧伤害倍率处理 (阳光剑) ===
        if (source.isFireDamage() && target.getEntityData().hasKey("chocotweak_sun_burn")) {
            damage *= 5.0f; // 燃烧伤害×5
        }

        event.setAmount(damage);
    }

    // ========== 实体更新事件 - 持续效果 ==========
    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();

        // 处理深渊冻结效果 (行者剑)
        if (entity.getEntityData().hasKey("chocotweak_abyss_freeze")) {
            long freezeEnd = entity.getEntityData().getLong("chocotweak_abyss_freeze");
            if (System.currentTimeMillis() < freezeEnd) {
                // 持续冻结移动
                entity.motionX = 0;
                entity.motionY = Math.min(entity.motionY, 0); // 允许下落但不能跳跃
                entity.motionZ = 0;
                entity.velocityChanged = true;
            } else {
                entity.getEntityData().removeTag("chocotweak_abyss_freeze");
            }
        }

        // 处理混乱效果 (月光剑) - 定期重新选择攻击目标
        if (entity.getEntityData().hasKey("chocotweak_chaos_end")) {
            long chaosEnd = entity.getEntityData().getLong("chocotweak_chaos_end");
            if (System.currentTimeMillis() < chaosEnd) {
                if (entity instanceof net.minecraft.entity.EntityCreature && entity.ticksExisted % 40 == 0) {
                    net.minecraft.entity.EntityCreature creature = (net.minecraft.entity.EntityCreature) entity;
                    // 每2秒随机切换目标
                    java.util.List<EntityLivingBase> nearbyMobs = entity.world.getEntitiesWithinAABB(
                            EntityLivingBase.class,
                            entity.getEntityBoundingBox().grow(10.0),
                            e -> e != entity && e.isEntityAlive() && !(e instanceof EntityPlayer));
                    if (!nearbyMobs.isEmpty()) {
                        EntityLivingBase newTarget = nearbyMobs.get(random.nextInt(nearbyMobs.size()));
                        creature.setAttackTarget(newTarget);
                    }
                }
            } else {
                entity.getEntityData().removeTag("chocotweak_chaos_end");
            }
        }

        // 玩家特殊效果
        if (!(entity instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) entity;
        ItemStack mainHand = player.getHeldItemMainhand();
        if (mainHand.isEmpty())
            return;

        Item item = mainHand.getItem();

        // === 龟盾 - 格挡时超高频回血 ===
        if (isItem(item, SWORD_TURTLE)) {
            if (player.isActiveItemStackBlocking() && player.ticksExisted % 10 == 0) {
                // 每0.5秒回复1心
                player.heal(2.0f);
            }
        }

        // === 阳光剑 - 标记燃烧伤害倍率 ===
        if (isItem(item, SWORD_SUNLIGHT)) {
            if (player.isBurning()) {
                player.getEntityData().setBoolean("chocotweak_sun_burn", true);
            } else {
                player.getEntityData().removeTag("chocotweak_sun_burn");
            }
        }
    }

    // ========== 辅助方法 ==========

    private boolean isDaytime(World world) {
        long time = world.getWorldTime() % 24000;
        return time < 12000; // 0-12000为白天
    }

    private void applyWoundEffect(EntityLivingBase target) {
        // 挫伤效果 - 减少无敌帧
        int currentLevel = 0;
        if (target.getEntityData().hasKey("chocotweak_wound_level")) {
            currentLevel = target.getEntityData().getInteger("chocotweak_wound_level");
        }

        // 最高3级
        int newLevel = Math.min(currentLevel + 1, 3);
        target.getEntityData().setInteger("chocotweak_wound_level", newLevel);
        target.getEntityData().setLong("chocotweak_wound_time", System.currentTimeMillis() + 10000); // 10秒持续

        // 减少无敌帧 (每级减2帧)
        target.hurtResistantTime = Math.max(0, target.hurtResistantTime - newLevel * 2);
    }

    /**
     * 应用蜘蛛剑随机负面效果
     */
    public static void applyRandomDebuff(EntityLivingBase target) {
        int effect = random.nextInt(6);
        switch (effect) {
            case 0:
                target.addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 1));
                break;
            case 1:
                target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 2));
                break;
            case 2:
                target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 1));
                break;
            case 3:
                target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 60, 0));
                break;
            case 4:
                target.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 200, 2));
                break;
            case 5:
                target.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 100, 1));
                break;
        }
    }
}
