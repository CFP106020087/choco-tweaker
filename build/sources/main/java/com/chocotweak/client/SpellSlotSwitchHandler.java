package com.chocotweak.client;

import com.chocotweak.ChocoTweak;
import com.chocotweak.network.ChocoNetwork;
import com.chocotweak.network.PacketSwapSpellSlot;
import com.chocolate.chocolateQuest.gui.InventoryBag;
import com.chocolate.chocolateQuest.items.ItemStaffBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

/**
 * 魔法槽位切换按键处理器
 * 按下按键时，将法杖/念珠的第一格魔法与下一格交换
 */
@Mod.EventBusSubscriber(modid = ChocoTweak.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class SpellSlotSwitchHandler {

    public static KeyBinding KEY_SWAP_SPELL;

    public static void registerKeybinds() {
        KEY_SWAP_SPELL = new KeyBinding(
                "key.chocotweak.swap_spell",
                Keyboard.KEY_V, // 默认按键 V
                "key.categories.chocotweak"
        );
        ClientRegistry.registerKeyBinding(KEY_SWAP_SPELL);
        ChocoTweak.LOGGER.info("[ChocoTweak] Registered spell swap keybind");
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KEY_SWAP_SPELL == null || !KEY_SWAP_SPELL.isPressed()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) {
            return;
        }

        ItemStack held = player.getHeldItemMainhand();
        if (held.isEmpty() || !(held.getItem() instanceof ItemStaffBase)) {
            return;
        }

        // 获取法术槽位
        ItemStack[] spells = InventoryBag.getCargo(held);
        if (spells.length < 2) {
            return; // 至少需要2个槽位才能交换
        }

        // 找到下一个非空槽位进行交换
        int swapIndex = -1;
        for (int i = 1; i < spells.length; i++) {
            if (!spells[i].isEmpty()) {
                swapIndex = i;
                break;
            }
        }

        if (swapIndex == -1) {
            return; // 没有可交换的法术
        }

        // 客户端本地预览交换 (可选)
        // 发送网络包到服务端执行实际交换
        ChocoNetwork.INSTANCE.sendToServer(new PacketSwapSpellSlot(swapIndex));
    }
}
