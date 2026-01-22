package com.chocotweak.weapon;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 武器属性修改器
 * 在mod初始化时通过反射直接修改CQ武器的白值
 * 使用注册名而非直接类引用避免类加载问题
 */
public class WeaponStatModifier {

    // 缓存的反射字段
    private static Field DAMAGE_FIELD = null;
    private static boolean fieldCached = false;

    // 攻速修改UUID
    private static final UUID CUSTOM_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    // 武器攻速配置 (负值越小越快，默认剑是-2.4)
    private static final Map<Item, Double> WEAPON_ATTACK_SPEEDS = new HashMap<>();

    // 通过注册名获取物品
    private static Item getItem(String registryName) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
    }

    /**
     * 在mod初始化后调用，修改所有CQ武器的基础属性
     */
    public static void applyCustomStats() {
        System.out.println("[ChocoTweak] Applying custom weapon stats...");

        try {
            // 尝试获取 ItemCQBlade 类和字段
            Class<?> bladeClass = Class.forName("com.chocolate.chocolateQuest.items.swords.ItemCQBlade");
            DAMAGE_FIELD = bladeClass.getDeclaredField("weaponAttackDamage");
            DAMAGE_FIELD.setAccessible(true);
            fieldCached = true;
            System.out.println("[ChocoTweak] Cached weaponAttackDamage field");

            // === 行者之剑 - 20伤害，高攻速 ===
            Item endSword = getItem("chocolatequest:endsword");
            if (endSword != null) {
                DAMAGE_FIELD.setFloat(endSword, 20.0f);
                WEAPON_ATTACK_SPEEDS.put(endSword, -2.0);
                System.out.println("[ChocoTweak] Set endSword: damage=20.0, speed=-2.0");
            }

            // === 龟盾 - 25伤害，很慢攻速 ===
            Item swordTurtle = getItem("chocolatequest:swordturtle");
            if (swordTurtle != null) {
                DAMAGE_FIELD.setFloat(swordTurtle, 25.0f);
                WEAPON_ATTACK_SPEEDS.put(swordTurtle, -3.2);
                System.out.println("[ChocoTweak] Set swordTurtle: damage=25.0, speed=-3.2");
            }

            // === 阳光剑 - 10伤害 ===
            Item swordSunLight = getItem("chocolatequest:swordsunlight");
            if (swordSunLight != null) {
                DAMAGE_FIELD.setFloat(swordSunLight, 10.0f);
                WEAPON_ATTACK_SPEEDS.put(swordSunLight, -2.6);
                System.out.println("[ChocoTweak] Set swordSunLight: damage=10.0, speed=-2.6");
            }

            // === 月光剑 - 10伤害 ===
            Item swordMoonLight = getItem("chocolatequest:moonsword");
            if (swordMoonLight != null) {
                DAMAGE_FIELD.setFloat(swordMoonLight, 10.0f);
                WEAPON_ATTACK_SPEEDS.put(swordMoonLight, -2.6);
                System.out.println("[ChocoTweak] Set swordMoonLight: damage=10.0, speed=-2.6");
            }

            // === 蜘蛛剑 - 8伤害，快攻速 ===
            Item swordSpider = getItem("chocolatequest:swordspider");
            if (swordSpider != null) {
                DAMAGE_FIELD.setFloat(swordSpider, 8.0f);
                WEAPON_ATTACK_SPEEDS.put(swordSpider, -2.2);
                System.out.println("[ChocoTweak] Set swordSpider: damage=8.0, speed=-2.2");
            }

            // === 锈剑 - 6伤害 ===
            Item rustedSword = getItem("chocolatequest:rustedswordandshied");
            if (rustedSword != null) {
                DAMAGE_FIELD.setFloat(rustedSword, 6.0f);
                WEAPON_ATTACK_SPEEDS.put(rustedSword, -2.6);
                System.out.println("[ChocoTweak] Set rustedSwordAndShied: damage=6.0, speed=-2.6");
            }

            // === 铁剑盾 - 7伤害 ===
            Item ironSword = getItem("chocolatequest:ironswordandshield");
            if (ironSword != null) {
                DAMAGE_FIELD.setFloat(ironSword, 7.0f);
                WEAPON_ATTACK_SPEEDS.put(ironSword, -2.6);
                System.out.println("[ChocoTweak] Set ironSwordAndShield: damage=7.0, speed=-2.6");
            }

            // === 钻石剑盾 - 9伤害 ===
            Item diamondSword = getItem("chocolatequest:diamondswordandshield");
            if (diamondSword != null) {
                DAMAGE_FIELD.setFloat(diamondSword, 9.0f);
                WEAPON_ATTACK_SPEEDS.put(diamondSword, -2.6);
                System.out.println("[ChocoTweak] Set diamondSwordAndShield: damage=9.0, speed=-2.6");
            }

            System.out.println("[ChocoTweak] Custom weapon stats applied successfully!");

        } catch (ClassNotFoundException e) {
            System.out.println("[ChocoTweak] CQ weapon classes not found, skipping weapon stat modification");
        } catch (NoClassDefFoundError e) {
            System.out.println("[ChocoTweak] CQ classes not available, skipping weapon stat modification");
        } catch (NoSuchFieldException e) {
            System.err.println("[ChocoTweak] Failed to find weaponAttackDamage field: " + e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println("[ChocoTweak] Failed to access weaponAttackDamage field: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ChocoTweak] Failed to apply weapon stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取武器的自定义攻速
     */
    public static Double getCustomAttackSpeed(Item item) {
        return WEAPON_ATTACK_SPEEDS.get(item);
    }

    /**
     * 装备变更事件 - 动态应用攻速修改
     */
    @SubscribeEvent
    public void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        // 只处理主手
        if (event.getSlot() != net.minecraft.inventory.EntityEquipmentSlot.MAINHAND)
            return;

        IAttributeInstance attackSpeed = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        if (attackSpeed == null)
            return;

        // 移除旧的自定义攻速修改
        AttributeModifier oldMod = attackSpeed.getModifier(CUSTOM_ATTACK_SPEED_UUID);
        if (oldMod != null) {
            attackSpeed.removeModifier(oldMod);
        }

        // 应用新武器的攻速
        ItemStack newStack = event.getTo();
        if (!newStack.isEmpty()) {
            Double customSpeed = WEAPON_ATTACK_SPEEDS.get(newStack.getItem());
            if (customSpeed != null) {
                // 计算相对于默认-2.4的差值
                double speedDiff = customSpeed - (-2.4);
                if (Math.abs(speedDiff) > 0.01) {
                    attackSpeed.applyModifier(new AttributeModifier(
                            CUSTOM_ATTACK_SPEED_UUID,
                            "ChocoTweak Attack Speed",
                            speedDiff,
                            0 // 加法模式
                    ));
                }
            }
        }
    }

    /**
     * PlayerTickEvent - 每tick检查并应用攻速修改
     */
    @SubscribeEvent
    public void onPlayerTick(net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.START)
            return;

        EntityPlayer player = event.player;
        ItemStack mainHand = player.getHeldItemMainhand();

        IAttributeInstance attackSpeed = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED);
        if (attackSpeed == null)
            return;

        // 移除旧的自定义攻速修改
        AttributeModifier oldMod = attackSpeed.getModifier(CUSTOM_ATTACK_SPEED_UUID);

        if (mainHand.isEmpty()) {
            if (oldMod != null)
                attackSpeed.removeModifier(oldMod);
            return;
        }

        Double customSpeed = WEAPON_ATTACK_SPEEDS.get(mainHand.getItem());

        if (customSpeed != null) {
            double speedDiff = customSpeed - (-2.4);
            if (Math.abs(speedDiff) > 0.01) {
                if (oldMod == null || Math.abs(oldMod.getAmount() - speedDiff) > 0.01) {
                    if (oldMod != null)
                        attackSpeed.removeModifier(oldMod);
                    attackSpeed.applyModifier(new AttributeModifier(
                            CUSTOM_ATTACK_SPEED_UUID, "ChocoTweak Attack Speed", speedDiff, 0));
                }
            }
        } else if (oldMod != null) {
            attackSpeed.removeModifier(oldMod);
        }
    }
}
