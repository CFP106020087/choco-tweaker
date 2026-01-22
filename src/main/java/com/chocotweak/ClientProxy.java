package com.chocotweak;

import com.chocotweak.client.GuiTranslationHandler;
import com.chocotweak.command.CommandChocoTweakClient;
import com.chocotweak.gui.GuiSelectCreature;
import com.chocotweak.gui.GuiSelectEnchantment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

/**
 * 客户端代理 - 处理快捷键和客户端命令
 */
@Mod.EventBusSubscriber(modid = ChocoTweak.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientProxy {

    // 快捷键绑定 - 默认绑定到 J 和 K 键方便测试
    public static KeyBinding keyCreatureGui;
    public static KeyBinding keyEnchantGui;
    public static KeyBinding keyBackpackGui;

    public static void registerKeyBindings() {
        ChocoTweak.LOGGER.info("Registering ChocoTweak client key bindings...");

        // 注册快捷键 - 默认 J 键打开生物选择器，K 键打开附魔选择器
        keyCreatureGui = new KeyBinding(
                "key.chocotweak.creature_gui",
                Keyboard.KEY_J, // 默认 J 键
                "key.categories.chocotweak");
        keyEnchantGui = new KeyBinding(
                "key.chocotweak.enchant_gui",
                Keyboard.KEY_K, // 默认 K 键
                "key.categories.chocotweak");
        keyBackpackGui = new KeyBinding(
                "key.chocotweak.backpack_gui",
                Keyboard.KEY_B, // 默认 B 键
                "key.categories.chocotweak");

        ClientRegistry.registerKeyBinding(keyCreatureGui);
        ClientRegistry.registerKeyBinding(keyEnchantGui);
        ClientRegistry.registerKeyBinding(keyBackpackGui);

        // 注册客户端命令
        try {
            ClientCommandHandler.instance.registerCommand(new CommandChocoTweakClient());
            ChocoTweak.LOGGER.info("Registered /ctc client command");
        } catch (Exception e) {
            ChocoTweak.LOGGER.error("Failed to register client command", e);
        }

        // 注册 GUI 翻译事件处理器
        MinecraftForge.EVENT_BUS.register(new GuiTranslationHandler());
        ChocoTweak.LOGGER.info("Registered GuiTranslationHandler for NPC editor translation");

        // 注册武器Tooltip事件处理器
        MinecraftForge.EVENT_BUS.register(new com.chocotweak.client.WeaponTooltipHandler());
        ChocoTweak.LOGGER.info("Registered WeaponTooltipHandler for weapon effect tooltips");

        // 注册法术槽位切换按键
        com.chocotweak.client.SpellSlotSwitchHandler.registerKeybinds();

        ChocoTweak.LOGGER.info("ChocoTweak client-side key bindings registered (J=Creature, K=Enchant, V=SwapSpell)");
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        // 检查玩家是否存在且没有打开其他界面
        if (mc.player == null || mc.currentScreen != null) {
            return;
        }

        // 检测按键
        if (keyCreatureGui != null && keyCreatureGui.isPressed()) {
            // 创造模式限制
            if (mc.player.isCreative()) {
                ChocoTweak.LOGGER.info("Opening creature selection GUI...");
                mc.displayGuiScreen(new GuiSelectCreature(null));
            }
        }

        if (keyEnchantGui != null && keyEnchantGui.isPressed()) {
            // 创造模式限制
            if (mc.player.isCreative()) {
                ChocoTweak.LOGGER.info("Opening enchantment selection GUI...");
                mc.displayGuiScreen(new GuiSelectEnchantment(null));
            }
        }

        // 背包快捷键 - 任何模式都可以使用
        if (keyBackpackGui != null && keyBackpackGui.isPressed()) {
            net.minecraft.item.ItemStack backpack = com.chocotweak.gui.ChocoTweakGuiHandler.findBackpack(mc.player);
            if (!backpack.isEmpty()) {
                mc.player.openGui(ChocoTweak.instance, com.chocotweak.gui.ChocoTweakGuiHandler.GUI_BACKPACK,
                        mc.player.world, 0, 0, 0);
            }
        }
    }
}
