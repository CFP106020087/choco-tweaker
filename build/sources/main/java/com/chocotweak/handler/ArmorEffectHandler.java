package com.chocotweak.handler;

import com.chocolate.chocolateQuest.ChocolateQuest;
import com.chocolate.chocolateQuest.entity.boss.EntitySlimePart;
import com.chocolate.chocolateQuest.utils.PlayerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.UUID;

/**
 * ChocoTweak 护甲效果处理器
 * 
 * 龟甲套：每10秒回复0.5心，背后受伤减半，全套死亡保护(10分钟冷却)
 * 公牛套：每件+5攻击力，全套耐力恢复+冲刺伤害
 * 蜘蛛套：每件+10%移速，全套爬墙+摔落免疫+负面效果上限1级
 * 史莱姆套：每件+10生命+25%击退抗性，全套受伤生成巨型史莱姆
 */
@Mod.EventBusSubscriber(modid = "chocotweak")
public class ArmorEffectHandler {

    // 属性修改器 UUID
    private static final UUID BULL_ATTACK_UUID = UUID.fromString("9D7A9B3E-1234-5678-9ABC-DEF012345678");
    private static final UUID SPIDER_SPEED_UUID = UUID.fromString("8E6B8C2F-2345-6789-ABCD-EF0123456789");
    private static final UUID SLIME_HEALTH_UUID = UUID.fromString("7F5C7D1E-3456-789A-BCDE-F01234567890");
    private static final UUID SLIME_KB_UUID = UUID.fromString("6A4D6E0F-4567-89AB-CDEF-012345678901");

    // ===== 龟甲套常量 =====
    private static final int TURTLE_HEAL_INTERVAL = 200; // 每10秒 = 200 ticks
    private static final int TURTLE_DEATH_PROTECT_CD = 12000; // 10分钟 = 12000 ticks

    // ===== 每 Tick 更新事件 =====
    @SubscribeEvent
    public static void onLivingUpdate(LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        // === 龟甲套：每10秒回复0.5心 ===
        int turtlePieces = countTurtleArmor(player);
        if (turtlePieces > 0 && player.ticksExisted % TURTLE_HEAL_INTERVAL == 0) {
            player.heal(1.0f * turtlePieces); // 每件0.5心 = 1.0f
            if (player.world.isRemote) {
                player.world.spawnParticle(EnumParticleTypes.HEART,
                        player.posX, player.posY + player.height + 0.5, player.posZ,
                        0, 0.1, 0);
            }
        }

        // 龟甲套死亡保护冷却计时
        if (player.getEntityData().hasKey("chocotweak_turtle_cd")) {
            int cd = player.getEntityData().getInteger("chocotweak_turtle_cd");
            if (cd > 0) {
                player.getEntityData().setInteger("chocotweak_turtle_cd", cd - 1);
            }
        }

        // 龟甲套死亡保护激活时快速回血
        if (player.getEntityData().getBoolean("chocotweak_turtle_regen")) {
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(1.0f); // 每tick回0.5心
            } else {
                player.getEntityData().setBoolean("chocotweak_turtle_regen", false);
            }
        }

        // === 公牛套：全套效果 ===
        if (hasFullBullArmor(player)) {
            // 持续恢复耐力
            PlayerManager.addStamina(player, 0.1f);

            // 冲刺时速度II + 碰撞伤害
            if (player.isSprinting()) {
                player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 2, 1, true, false));

                // 对碰撞敌人造成伤害
                List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player,
                        player.getEntityBoundingBox().grow(0.5, 0.5, 0.5));
                for (Entity entity : entities) {
                    if (entity instanceof EntityLivingBase && !player.isOnSameTeam((EntityLivingBase) entity)) {
                        // 使用玩家攻击力作为伤害
                        float damage = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                                .getAttributeValue();
                        entity.attackEntityFrom(new EntityDamageSource(DamageSource.GENERIC.damageType, player),
                                damage);
                    }
                }
            }
        }

        // === 蜘蛛套：全套效果 ===
        if (hasFullSpiderArmor(player)) {
            // 爬墙能力
            if (player.collidedHorizontally) {
                if (!player.isSneaking()) {
                    player.motionY = 0.0;
                    if (player.moveForward > 0.0f) {
                        player.motionY = 0.2;
                    }
                }
                player.onGround = true;
            }
            player.fallDistance = 0.0f;
        }

        // === 史莱姆套：生命和击退抗性通过属性修改器处理 ===
        updateArmorAttributes(player);
    }

    // ===== 受伤事件 =====
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        float damage = event.getAmount();

        // === 龟甲套：背后受伤减半 ===
        if (hasTurtleChestplate(player) && event.getSource().getTrueSource() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
            if (isAttackFromBehind(player, attacker)) {
                event.setAmount(damage * 0.5f);
                damage = event.getAmount();
            }
        }

        // === 龟甲套：全套死亡保护 ===
        if (hasFullTurtleArmor(player)) {
            int cd = player.getEntityData().getInteger("chocotweak_turtle_cd");
            if (cd <= 0 && player.getHealth() - damage <= 0.0f) {
                // 触发死亡保护
                event.setCanceled(true);
                player.setHealth(0.1f);
                player.getEntityData().setInteger("chocotweak_turtle_cd", TURTLE_DEATH_PROTECT_CD);
                player.getEntityData().setBoolean("chocotweak_turtle_regen", true);
            }
        }

        // === 史莱姆套：全套生成巨型史莱姆 ===
        if (hasFullSlimeArmor(player) && damage > 0) {
            if (!player.world.isRemote && player.hurtTime == 0) {
                EntitySlimePart part = new EntitySlimePart(player.world, player, damage * 2.0f);
                part.setPosition(player.posX, player.posY + 1.0, player.posZ);
                part.motionX = player.world.rand.nextGaussian() * 0.5;
                part.motionY = 0.5 + player.world.rand.nextDouble() * 0.5;
                part.motionZ = player.world.rand.nextGaussian() * 0.5;
                player.world.spawnEntity(part);
            }
        }
    }

    // ===== 摔落事件 =====
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        // 蜘蛛套全套免疫摔落伤害
        if (hasFullSpiderArmor(player)) {
            event.setCanceled(true);
        }
    }

    // ===== 药水效果事件 =====
    @SubscribeEvent
    public static void onPotionAdd(PotionEvent.PotionAddedEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        // 蜘蛛套：负面效果最多1级
        if (hasFullSpiderArmor(player)) {
            PotionEffect effect = event.getPotionEffect();
            if (effect.getPotion().isBadEffect() && effect.getAmplifier() > 0) {
                // 替换为1级效果
                player.removeActivePotionEffect(effect.getPotion());
                player.addPotionEffect(new PotionEffect(effect.getPotion(), effect.getDuration(), 0));
            }
        }
    }

    // ===== 辅助方法 =====

    private static void updateArmorAttributes(EntityPlayer player) {
        IAttributeInstance attackAttr = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        IAttributeInstance speedAttr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        IAttributeInstance healthAttr = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        IAttributeInstance kbAttr = player.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);

        // 移除旧修改器
        attackAttr.removeModifier(BULL_ATTACK_UUID);
        speedAttr.removeModifier(SPIDER_SPEED_UUID);
        healthAttr.removeModifier(SLIME_HEALTH_UUID);
        kbAttr.removeModifier(SLIME_KB_UUID);

        // 公牛套：每件+5攻击力
        int bullPieces = countBullArmor(player);
        if (bullPieces > 0) {
            attackAttr.applyModifier(new AttributeModifier(BULL_ATTACK_UUID, "Bull Armor Attack", 5.0 * bullPieces, 0));
        }

        // 蜘蛛套：每件+10%移速
        int spiderPieces = countSpiderArmor(player);
        if (spiderPieces > 0) {
            speedAttr.applyModifier(
                    new AttributeModifier(SPIDER_SPEED_UUID, "Spider Armor Speed", 0.1 * spiderPieces, 2));
        }

        // 史莱姆套：每件+10生命 +25%击退抗性
        int slimePieces = countSlimeArmor(player);
        if (slimePieces > 0) {
            healthAttr.applyModifier(
                    new AttributeModifier(SLIME_HEALTH_UUID, "Slime Armor Health", 10.0 * slimePieces, 0));
            kbAttr.applyModifier(new AttributeModifier(SLIME_KB_UUID, "Slime Armor KB", 0.25 * slimePieces, 0));
        }
    }

    private static boolean isAttackFromBehind(EntityPlayer player, EntityLivingBase attacker) {
        double angle = player.rotationYaw - attacker.rotationYaw;
        while (angle > 360.0)
            angle -= 360.0;
        while (angle < 0.0)
            angle += 360.0;
        angle = Math.abs(angle - 180.0);
        return angle > 130.0;
    }

    // === 护甲计数方法 ===

    private static int countTurtleArmor(EntityPlayer player) {
        int count = 0;
        if (isItem(player, EntityEquipmentSlot.HEAD, ChocolateQuest.turtleHelmet))
            count++;
        if (isItem(player, EntityEquipmentSlot.CHEST, ChocolateQuest.turtlePlate))
            count++;
        if (isItem(player, EntityEquipmentSlot.LEGS, ChocolateQuest.turtlePants))
            count++;
        if (isItem(player, EntityEquipmentSlot.FEET, ChocolateQuest.turtleBoots))
            count++;
        return count;
    }

    private static boolean hasFullTurtleArmor(EntityPlayer player) {
        return countTurtleArmor(player) == 4;
    }

    private static boolean hasTurtleChestplate(EntityPlayer player) {
        return isItem(player, EntityEquipmentSlot.CHEST, ChocolateQuest.turtlePlate);
    }

    private static int countBullArmor(EntityPlayer player) {
        int count = 0;
        if (isItem(player, EntityEquipmentSlot.HEAD, ChocolateQuest.bullHelmet))
            count++;
        if (isItem(player, EntityEquipmentSlot.CHEST, ChocolateQuest.bullPlate))
            count++;
        if (isItem(player, EntityEquipmentSlot.LEGS, ChocolateQuest.bullPants))
            count++;
        if (isItem(player, EntityEquipmentSlot.FEET, ChocolateQuest.bullBoots))
            count++;
        return count;
    }

    private static boolean hasFullBullArmor(EntityPlayer player) {
        return countBullArmor(player) == 4;
    }

    private static int countSpiderArmor(EntityPlayer player) {
        int count = 0;
        if (isItem(player, EntityEquipmentSlot.HEAD, ChocolateQuest.spiderHelmet))
            count++;
        if (isItem(player, EntityEquipmentSlot.CHEST, ChocolateQuest.spiderPlate))
            count++;
        if (isItem(player, EntityEquipmentSlot.LEGS, ChocolateQuest.spiderPants))
            count++;
        if (isItem(player, EntityEquipmentSlot.FEET, ChocolateQuest.spiderBoots))
            count++;
        return count;
    }

    private static boolean hasFullSpiderArmor(EntityPlayer player) {
        return countSpiderArmor(player) == 4;
    }

    private static int countSlimeArmor(EntityPlayer player) {
        int count = 0;
        if (isItem(player, EntityEquipmentSlot.HEAD, ChocolateQuest.slimeHelmet))
            count++;
        if (isItem(player, EntityEquipmentSlot.CHEST, ChocolateQuest.slimePlate))
            count++;
        if (isItem(player, EntityEquipmentSlot.LEGS, ChocolateQuest.slimePants))
            count++;
        if (isItem(player, EntityEquipmentSlot.FEET, ChocolateQuest.slimeBoots))
            count++;
        return count;
    }

    private static boolean hasFullSlimeArmor(EntityPlayer player) {
        return countSlimeArmor(player) == 4;
    }

    private static boolean isItem(EntityPlayer player, EntityEquipmentSlot slot, Item item) {
        ItemStack stack = player.getItemStackFromSlot(slot);
        return !stack.isEmpty() && stack.getItem() == item;
    }
}
