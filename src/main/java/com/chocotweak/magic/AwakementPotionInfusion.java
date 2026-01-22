package com.chocotweak.magic;

import com.chocolate.chocolateQuest.items.swords.ItemCQBlade;
import com.chocolate.chocolateQuest.magic.Awakements;
import com.chocolate.chocolateQuest.misc.EnumEnchantType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;

import java.util.List;

/**
 * 药水灌注觉醒 - 从玩家背包消耗药水附加到武器
 * 
 * 效果：
 * - 消耗XP + 背包中的药水
 * - 将药水效果附加到武器上
 * - 武器命中时触发药水效果
 * 
 * 注意：这个觉醒不是传统的等级提升，而是消耗药水的操作
 */
public class AwakementPotionInfusion extends Awakements {

    /** 每次灌注消耗的XP等级 */
    public static final int XP_COST = 5;

    public AwakementPotionInfusion(String name, int icon) {
        super(name, icon);
    }

    @Override
    public boolean canBeUsedOnItem(ItemStack is) {
        // 只能用于CQ武器
        return is.getItem() instanceof ItemCQBlade;
    }

    @Override
    public int getLevelCost() {
        return XP_COST;
    }

    @Override
    public boolean canBeAddedByNPC(int type) {
        // 铁匠NPC提供此服务 (与其他武器觉醒一致)
        return type == EnumEnchantType.BLACKSMITH.ordinal();
    }

    @Override
    public int getMaxLevel() {
        // 可以多次灌注，最多3种药水效果
        return 3;
    }

    /**
     * 检查玩家背包是否有药水
     */
    public static ItemStack findPotionInInventory(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && isPotionItem(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 检查是否是药水物品
     */
    public static boolean isPotionItem(ItemStack stack) {
        if (stack.isEmpty())
            return false;

        // 原版药水
        if (stack.getItem() instanceof ItemPotion) {
            return true;
        }

        // 模组药水（通过NBT检测）
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            return tag.hasKey("Potion") || tag.hasKey("CustomPotionEffects");
        }

        return false;
    }

    /**
     * 执行药水灌注
     * 
     * @param weapon 目标武器
     * @param player 玩家
     * @return 是否成功
     */
    public static boolean infusePotion(ItemStack weapon, EntityPlayer player) {
        if (weapon.isEmpty() || !(weapon.getItem() instanceof ItemCQBlade)) {
            return false;
        }

        // 查找背包中的药水
        ItemStack potion = findPotionInInventory(player);
        if (potion.isEmpty()) {
            return false;
        }

        // 检查武器已有的药水效果数量
        int currentCount = getPotionEffectCount(weapon);
        int maxSlots = AwakementPotionCapacity.getPotionSlotsForWeapon(weapon);
        if (currentCount >= maxSlots) {
            return false;
        }

        // 检查XP
        if (!player.capabilities.isCreativeMode && player.experienceLevel < XP_COST) {
            return false;
        }

        // 获取药水效果
        List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potion);
        if (effects.isEmpty()) {
            return false;
        }

        // 获取效果增强倍率
        float bonus = AwakementPotionCapacity.getPotionBonus(
                Awakements.getEnchantLevel(weapon, com.chocotweak.core.AwakementsInitializer.potionCapacity));

        // 添加药水效果到武器
        addPotionEffectsToWeapon(weapon, effects, bonus);

        // 消耗XP
        if (!player.capabilities.isCreativeMode) {
            player.experienceLevel -= XP_COST;
        }

        // 消耗药水
        potion.shrink(1);

        return true;
    }

    /**
     * 将药水效果添加到武器NBT
     */
    private static void addPotionEffectsToWeapon(ItemStack weapon, List<PotionEffect> newEffects, float bonus) {
        if (!weapon.hasTagCompound()) {
            weapon.setTagCompound(new NBTTagCompound());
        }

        NBTTagList existingList;
        if (weapon.getTagCompound().hasKey("CustomPotionEffects", 9)) {
            existingList = weapon.getTagCompound().getTagList("CustomPotionEffects", 10);
        } else {
            existingList = new NBTTagList();
        }

        for (PotionEffect newEffect : newEffects) {
            // 检查是否已有同类效果（叠加）
            boolean found = false;
            for (int i = 0; i < existingList.tagCount(); i++) {
                NBTTagCompound existingNBT = existingList.getCompoundTagAt(i);
                PotionEffect existingEffect = PotionEffect.readCustomPotionEffectFromNBT(existingNBT);
                if (existingEffect != null && existingEffect.getPotion() == newEffect.getPotion()) {
                    // 叠加：增加持续时间
                    int newDuration = existingEffect.getDuration() + (int) (newEffect.getDuration() * bonus);
                    int newAmplifier = Math.max(existingEffect.getAmplifier(), newEffect.getAmplifier());

                    PotionEffect merged = new PotionEffect(
                            newEffect.getPotion(),
                            newDuration,
                            newAmplifier);

                    NBTTagCompound mergedNBT = new NBTTagCompound();
                    merged.writeCustomPotionEffectToNBT(mergedNBT);
                    existingList.set(i, mergedNBT);
                    found = true;
                    break;
                }
            }

            if (!found) {
                // 添加新效果
                int boostedDuration = (int) (newEffect.getDuration() * bonus);
                PotionEffect boosted = new PotionEffect(
                        newEffect.getPotion(),
                        boostedDuration,
                        newEffect.getAmplifier());

                NBTTagCompound effectNBT = new NBTTagCompound();
                boosted.writeCustomPotionEffectToNBT(effectNBT);
                existingList.appendTag(effectNBT);
            }
        }

        weapon.getTagCompound().setTag("CustomPotionEffects", existingList);
    }

    /**
     * 获取武器当前药水效果数量
     */
    public static int getPotionEffectCount(ItemStack weapon) {
        if (!weapon.hasTagCompound())
            return 0;
        if (!weapon.getTagCompound().hasKey("CustomPotionEffects", 9))
            return 0;
        return weapon.getTagCompound().getTagList("CustomPotionEffects", 10).tagCount();
    }
}
