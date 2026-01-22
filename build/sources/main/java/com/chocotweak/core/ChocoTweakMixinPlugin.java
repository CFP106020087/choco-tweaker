package com.chocotweak.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import fermiumbooter.FermiumRegistryAPI;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions({ "com.chocotweak.core" })
@IFMLLoadingPlugin.Name("ChocoTweakCore")
@IFMLLoadingPlugin.SortingIndex(1005)
public class ChocoTweakMixinPlugin implements IFMLLoadingPlugin {

    static {
        // FermiumBooter mixin 注册 - 使用 FermiumBooter 而非 MANIFEST TweakClass
        try {
            // Early mixin - 原版 Minecraft 类（如 PlayerControllerMP） - false = 早加载
            FermiumRegistryAPI.enqueueMixin(false, "chocotweak.early.mixins.json");
            System.out.println("[ChocoTweak] Early mixins queued via FermiumBooter");

            // Late mixin - Mod 类（ChocolateQuest 等） - true = 后加载
            FermiumRegistryAPI.enqueueMixin(true, "chocotweak.mixins.json");
            System.out.println("[ChocoTweak] Late mixins queued via FermiumBooter");
        } catch (Throwable e) {
            System.err.println("[ChocoTweak] FermiumBooter registration failed: " + e);
        }
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String[] getASMTransformerClass() {
        // ASM已移除，改用Mixin实现
        return new String[0];
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
