package com.chocotweak.mixin;

import com.chocotweak.core.ContainerAwakementHelper;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 捕获GuiAwakement中的按钮点击，在调用enchantItem之前设置pendingAwakementId
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.guinpc.GuiAwakement", remap = false)
public abstract class MixinGuiAwakementAction {

    /**
     * 在actionPerformed开始时，如果是觉醒模式，保存按钮ID到holder
     */
    @Inject(method = "actionPerformed", at = @At("HEAD"))
    private void onActionPerformedHead(GuiButton button, CallbackInfo ci) throws IOException {
        // 只处理觉醒/附魔按钮 (id < 256)
        if (button.id < 256) {
            // 尝试获取container的mode字段来判断是觉醒模式还是附魔模式
            boolean isEnchantMode = true;
            try {
                // inventorySlots 在父类 GuiContainer 中
                Field inventorySlotsField = findField(this.getClass(), "inventorySlots");
                if (inventorySlotsField == null) {
                    inventorySlotsField = findField(this.getClass(), "field_147002_h"); // obfuscated name
                }
                if (inventorySlotsField != null) {
                    inventorySlotsField.setAccessible(true);
                    Object container = inventorySlotsField.get(this);
                    if (container != null) {
                        Field modeField = container.getClass().getField("mode");
                        isEnchantMode = modeField.getBoolean(container);
                    }
                }
            } catch (Exception e) {
                // 如果获取失败，假设是觉醒模式
                isEnchantMode = false;
            }

            if (!isEnchantMode) {
                // 觉醒模式 - 保存按钮ID（就是觉醒ID）到helper
                ContainerAwakementHelper.pendingAwakementId = button.id;
                System.out.println("[ChocoTweak] Set pendingAwakementId = " + button.id);
            }
        }
    }

    /**
     * 递归查找字段（包括父类）
     */
    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}

