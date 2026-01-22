package com.chocotweak.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * 背包容量超大幅度提升
 * 
 * 原版: damage * 9 + 9 (最大约 27 格)
 * 修改: 固定 108 格 (12行 x 9列 = 2个大箱子容量)
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.InventoryBag", remap = false)
public class MixinBagCapacity {

    // 固定大容量：108 格 = 12 行 x 9 列 (相当于两个大箱子)
    private static final int MEGA_CAPACITY = 108;

    /**
     * 检查物品是否实现 ILoadableGun 接口
     */
    private static boolean isLoadableGun(Item item) {
        for (Class<?> iface : item.getClass().getInterfaces()) {
            if (iface.getName().equals("com.chocolate.chocolateQuest.items.gun.ILoadableGun")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过反射调用 getAmmoLoaderAmmount
     */
    private static int getAmmoLoaderAmmount(Item item, ItemStack is) {
        try {
            java.lang.reflect.Method method = item.getClass().getMethod("getAmmoLoaderAmmount", ItemStack.class);
            return (Integer) method.invoke(item, is);
        } catch (Exception e) {
            return MEGA_CAPACITY;
        }
    }

    /**
     * @author ChocoTweak
     * @reason 超大幅度提升背包容量
     */
    @Overwrite
    public static int getSizeInventory(ItemStack is) {
        if (isLoadableGun(is.getItem())) {
            // 弹药包保持原逻辑
            return getAmmoLoaderAmmount(is.getItem(), is);
        }

        // 背包容量超大幅度提升：固定 108 格
        return MEGA_CAPACITY;
    }
}


