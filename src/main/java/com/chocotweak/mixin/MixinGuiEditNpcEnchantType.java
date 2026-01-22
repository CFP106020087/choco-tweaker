package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.entity.npc.EntityHumanNPC;
import com.chocolate.chocolateQuest.gui.GuiButtonDisplayString;
import com.chocolate.chocolateQuest.gui.GuiButtonMultiOptions;
import com.chocolate.chocolateQuest.misc.EnumEnchantType;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 在NPC编辑界面添加职业类型(enchantType)选项
 * 
 * 原版CQ的GuiEditNpc没有这个选项,导致无法设置NPC为铁匠等职业
 */
@SideOnly(Side.CLIENT)
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.guinpc.GuiEditNpc", remap = false)
public abstract class MixinGuiEditNpcEnchantType {

    @Shadow
    EntityHumanNPC npc;

    @Shadow
    protected List<GuiButton> buttonList;

    @Unique
    private GuiButtonMultiOptions chocotweak$enchantTypeButton;

    /**
     * 在initGui结束时添加enchantType选项
     */
    @Inject(method = "initGui", at = @At("TAIL"))
    private void onInitGui(CallbackInfo ci) {
        try {
            // 获取当前enchantType值
            int currentEnchantType = getEnchantType();
            
            // 创建选项列表
            String[] enchantTypeNames = new String[] {
                "Enchanter",    // 0 - 附魔师
                "Blacksmith",   // 1 - 铁匠 (药水灌注)
                "Gunsmith",     // 2 - 枪匠
                "Staves",       // 3 - 法杖商
                "Tailor"        // 4 - 裁缝
            };
            
            // 计算位置 (放在Health/Speed那一行下面)
            int x = 25;
            int y = 310; // 放在Home X/Y/Z下面
            int buttonWidth = 100;
            int buttonHeight = 20;
            
            // 添加标签
            GuiButtonDisplayString label = new GuiButtonDisplayString(0, x, y, buttonWidth, buttonHeight, 
                    "Enchant Type");
            buttonList.add(label);
            
            // 添加选择按钮
            chocotweak$enchantTypeButton = new GuiButtonMultiOptions(9999, x, y + buttonHeight, 
                    buttonWidth, buttonHeight, enchantTypeNames, 
                    Math.min(currentEnchantType, enchantTypeNames.length - 1));
            buttonList.add(chocotweak$enchantTypeButton);
            
        } catch (Exception e) {
            com.chocotweak.ChocoTweak.LOGGER.warn("[ChocoTweak] Failed to add enchantType button", e);
        }
    }

    /**
     * 在updateNPC时保存enchantType
     */
    @Inject(method = "updateNPC", at = @At("TAIL"))
    private void onUpdateNPC(CallbackInfo ci) {
        try {
            if (chocotweak$enchantTypeButton != null && npc != null) {
                setEnchantType(chocotweak$enchantTypeButton.value);
            }
        } catch (Exception e) {
            com.chocotweak.ChocoTweak.LOGGER.warn("[ChocoTweak] Failed to save enchantType", e);
        }
    }

    /**
     * 通过反射获取NPC的enchantType
     */
    @Unique
    private int getEnchantType() {
        try {
            Field field = npc.getClass().getField("enchantType");
            return field.getInt(npc);
        } catch (NoSuchFieldException e) {
            // 字段可能在父类
            try {
                Field field = npc.getClass().getSuperclass().getField("enchantType");
                return field.getInt(npc);
            } catch (Exception e2) {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 通过反射设置NPC的enchantType
     */
    @Unique
    private void setEnchantType(int value) {
        try {
            Field field = npc.getClass().getField("enchantType");
            field.setInt(npc, value);
        } catch (NoSuchFieldException e) {
            try {
                Field field = npc.getClass().getSuperclass().getField("enchantType");
                field.setInt(npc, value);
            } catch (Exception e2) {
                com.chocotweak.ChocoTweak.LOGGER.warn("[ChocoTweak] Cannot find enchantType field");
            }
        } catch (Exception e) {
            com.chocotweak.ChocoTweak.LOGGER.warn("[ChocoTweak] Cannot set enchantType", e);
        }
    }
}
