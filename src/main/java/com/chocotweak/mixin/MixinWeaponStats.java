package com.chocotweak.mixin;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

/**
 * 武器基础属性Mixin
 * 通过@Inject修改getAttributeModifiers返回值，添加攻速和伤害覆盖
 * 同时注入tooltip显示自定义攻速和伤害信息
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.swords.ItemCQBlade", remap = false)
public abstract class MixinWeaponStats {

    private static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

    @Shadow
    protected float weaponAttackDamage;

    /**
     * 注入getAttributeModifiers在RETURN时添加攻速和伤害覆盖
     */
    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true, remap = true)
    private void addCustomAttributes(EntityEquipmentSlot slot, ItemStack stack,
            CallbackInfoReturnable<Multimap<String, AttributeModifier>> cir) {

        if (slot != EntityEquipmentSlot.MAINHAND)
            return;

        Item item = stack.getItem();
        Double customSpeed = getCustomAttackSpeed(item);
        Float customDamage = getCustomDamage(item);

        if (customSpeed != null || customDamage != null) {
            // 创建可变的副本
            Multimap<String, AttributeModifier> mutableMap = HashMultimap.create(cir.getReturnValue());

            // 攻速覆盖
            if (customSpeed != null) {
                mutableMap.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());
                mutableMap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                        new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", customSpeed, 0));
            }

            // 伤害覆盖
            if (customDamage != null) {
                mutableMap.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
                mutableMap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                        new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", customDamage, 0));
            }

            // 设置新的返回值
            cir.setReturnValue(mutableMap);
        }
    }

    /**
     * 注入addInformation在末尾添加攻速信息到tooltip
     */
    @Inject(method = "addInformation", at = @At("RETURN"), remap = true)
    private void addCustomTooltip(ItemStack is, World worldIn, List<String> list, ITooltipFlag flagIn,
            CallbackInfo ci) {
        Item item = is.getItem();
        Double customSpeed = getCustomAttackSpeed(item);
        Float customDamage = getCustomDamage(item);

        if (customSpeed != null || customDamage != null) {
            list.add("");

            // 显示自定义伤害
            if (customDamage != null) {
                list.add(TextFormatting.DARK_AQUA + "ChocoTweak: " + TextFormatting.GRAY + "攻击 " + TextFormatting.RED
                        + String.format("%.0f", customDamage));
            }

            // 显示自定义攻速
            if (customSpeed != null) {
                double displaySpeed = 4.0 + customSpeed;
                String speedText = String.format("%.1f", displaySpeed);
                list.add(TextFormatting.DARK_AQUA + "ChocoTweak: " + TextFormatting.GRAY + "攻速 " + TextFormatting.WHITE
                        + speedText);
            }
        }
    }

    private static boolean isItem(Item item, String registryName) {
        ResourceLocation loc = item.getRegistryName();
        return loc != null && loc.toString().equals(registryName);
    }

    private static Double getCustomAttackSpeed(Item item) {
        // === 行者之剑 - 高攻速 ===
        if (isItem(item, "chocolatequest:endsword"))
            return -2.0;
        // === 龟盾 - 极慢攻速 ===
        if (isItem(item, "chocolatequest:swordturtle"))
            return -3.2;
        // === 蜘蛛剑 - 快攻速 ===
        if (isItem(item, "chocolatequest:swordspider"))
            return -2.2;
        // === 阳光剑 ===
        if (isItem(item, "chocolatequest:swordsunlight"))
            return -2.6;
        // === 月光剑 ===
        if (isItem(item, "chocolatequest:moonsword"))
            return -2.6;
        // === 锈剑 ===
        if (isItem(item, "chocolatequest:rustedswordandshied"))
            return -2.6;
        // === 铁剑盾 ===
        if (isItem(item, "chocolatequest:ironswordandshield"))
            return -2.6;
        // === 钻石剑盾 ===
        if (isItem(item, "chocolatequest:diamondswordandshield"))
            return -2.6;
        // === 猴王剑盾 - 极慢攻速 ===
        if (isItem(item, "chocolatequest:swordshiedmonking"))
            return -3.5;
        // === 猴王大剑 ===
        if (isItem(item, "chocolatequest:swordmonking"))
            return -3.2;
        // === 钩剑 ===
        if (isItem(item, "chocolatequest:hooksword"))
            return -2.4;

        return null;
    }

    /**
     * 获取自定义伤害值
     */
    private static Float getCustomDamage(Item item) {
        // === 猴王剑盾 - 30伤害 ===
        if (isItem(item, "chocolatequest:swordshiedmonking"))
            return 30.0F;
        // === 猴王大剑 - 35伤害 ===
        if (isItem(item, "chocolatequest:swordmonking"))
            return 35.0F;

        return null;
    }
}
