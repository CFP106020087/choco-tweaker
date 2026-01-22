package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.EventHandlerCQ;
import com.chocolate.chocolateQuest.items.ItemElementStone;
import com.chocolate.chocolateQuest.magic.Elements;
import com.chocotweak.ChocoTweak;
import com.chocotweak.config.CQTweakConfig;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraftforge.event.AnvilUpdateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截 EventHandlerCQ 的铁砧事件，允许元素石应用于任意模组武器/护甲
 * 
 * 功能:
 * 1. 当 universalCompatibility 启用时，允许任意 ItemSword, ItemAxe, ItemTool, ItemArmor
 * 使用元素石
 * 2. 使用配置中的自定义 XP 消耗和最大等级
 * 3. 复用 CQ 的 Elements NBT 存储系统
 */
@Mixin(value = EventHandlerCQ.class, remap = false)
public class MixinEventHandlerCQ {

    @Unique
    private static boolean chocotweak$logged = false;

    /**
     * 在原版铁砧事件处理前拦截，为非CQ物品提供元素石支持
     */
    @Inject(method = "onAnvilUpdateEvent", at = @At("HEAD"), cancellable = true)
    private void chocotweak$universalElementStone(AnvilUpdateEvent event, CallbackInfo ci) {
        // 仅当配置启用时生效
        if (!CQTweakConfig.elementStone.universalCompatibility) {
            return;
        }

        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        // 检查右侧是否为元素石
        if (left.isEmpty() || right.isEmpty()) {
            return;
        }
        if (!(right.getItem() instanceof ItemElementStone)) {
            return;
        }

        // 检查左侧是否已经是CQ物品 - 如果是，让原版处理器处理
        if (chocotweak$isCQItem(left)) {
            return;
        }

        // 检查左侧是否为武器或护甲
        boolean isWeapon = chocotweak$isWeapon(left);
        boolean isArmor = left.getItem() instanceof ItemArmor;

        if (!isWeapon && !isArmor) {
            return;
        }

        // 日志记录 (仅一次)
        if (!chocotweak$logged) {
            ChocoTweak.LOGGER.info("[ChocoTweak] Universal Element Stone compatibility active");
            chocotweak$logged = true;
        }

        // 获取元素和当前等级
        ItemElementStone elementStone = (ItemElementStone) right.getItem();
        Elements element = elementStone.getElement(right);
        int currentLevel = Elements.getElementValue(left, element);

        // 确定最大等级 (取配置值和石头等级的较小值)
        int stoneMaxLevel = elementStone.getMaxLevel(right);
        int configMaxLevel = isArmor ? CQTweakConfig.elementStone.modArmorMaxLevel
                : CQTweakConfig.elementStone.modWeaponMaxLevel;
        int maxLevel = Math.min(stoneMaxLevel, configMaxLevel);

        // 检查是否已达最大等级
        if (currentLevel >= maxLevel) {
            return;
        }

        // 创建输出物品
        ItemStack output = left.copy();
        Elements.setElementValue(output, element, currentLevel + 1);
        event.setOutput(output);

        // 计算 XP 消耗
        int baseCost = isArmor ? CQTweakConfig.elementStone.modArmorBaseCost
                : CQTweakConfig.elementStone.modWeaponBaseCost;
        int cost = baseCost + currentLevel * CQTweakConfig.elementStone.costPerLevel;
        event.setCost(cost);

        // 取消原版处理
        ci.cancel();
    }

    /**
     * 检查物品是否为CQ原版物品 (让原版处理器处理)
     */
    @Unique
    private boolean chocotweak$isCQItem(ItemStack stack) {
        String className = stack.getItem().getClass().getName();
        return className.contains("chocolate") || className.contains("chocolateQuest");
    }

    /**
     * 检查物品是否为武器类型
     */
    @Unique
    private boolean chocotweak$isWeapon(ItemStack stack) {
        // 标准剑类
        if (stack.getItem() instanceof ItemSword) {
            return true;
        }
        // 斧头 (常被用作武器)
        if (stack.getItem() instanceof ItemAxe) {
            return true;
        }
        // 部分模组使用 ItemTool 作为武器基类
        if (stack.getItem() instanceof ItemTool) {
            // 排除纯工具类 (镐、锹)
            String name = stack.getItem().getClass().getSimpleName().toLowerCase();
            if (name.contains("pick") || name.contains("shovel") || name.contains("spade")) {
                return false;
            }
            // 包含 weapon, sword, blade, axe 等关键字的视为武器
            return name.contains("weapon") || name.contains("sword") || name.contains("blade")
                    || name.contains("axe") || name.contains("halberd") || name.contains("mace")
                    || name.contains("hammer") || name.contains("scythe") || name.contains("staff")
                    || name.contains("katana") || name.contains("rapier") || name.contains("dagger");
        }
        return false;
    }
}
