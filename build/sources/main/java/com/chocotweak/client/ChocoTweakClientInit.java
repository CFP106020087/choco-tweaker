package com.chocotweak.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

/**
 * ChocoTweak 客户端初始化
 * 注册饰品渲染层
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "chocotweak", value = Side.CLIENT)
public class ChocoTweakClientInit {

    private static boolean initialized = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!initialized && event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getRenderManager() != null) {
                Map<String, RenderPlayer> skinMap = mc.getRenderManager().getSkinMap();
                if (skinMap != null && !skinMap.isEmpty()) {
                    registerAccessoryLayers();
                    initialized = true;
                }
            }
        }
    }

    private static void registerAccessoryLayers() {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        Map<String, RenderPlayer> skinMap = renderManager.getSkinMap();

        for (RenderPlayer renderPlayer : skinMap.values()) {
            renderPlayer.addLayer(new AccessoryRenderLayer(renderPlayer));
        }
    }
}
