package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.entity.npc.EntityHumanNPC;
import com.chocolate.chocolateQuest.gui.GuiButtonMultiOptions;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import com.chocotweak.network.ChocoNetwork;
import com.chocotweak.network.PacketEasterEggSync;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to add Easter Egg options to GuiEditNpc "Texture type" selector.
 * Adds EasterEgg0, EasterEgg1, etc. options for wine_fox model variants.
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.gui.guinpc.GuiEditNpc", remap = false)
public abstract class MixinGuiEditNpc extends GuiScreen {

    @Shadow
    EntityHumanNPC npc;
    @Shadow
    GuiButtonMultiOptions usePlayerTexture;

    @Unique
    private static final String[] TEXTURE_TYPES = {
            "Player", "Entity",
            "EasterEgg0", "EasterEgg1", "EasterEgg2", "EasterEgg3", "EasterEgg4",
            "EasterEgg5", "EasterEgg6", "EasterEgg7", "EasterEgg8", "EasterEgg9",
            "EasterEgg10", "EasterEgg11", "EasterEgg12", "EasterEgg13", "EasterEgg14"
    };

    /**
     * After initGui sets up usePlayerTexture, replace it with extended options.
     */
    @Inject(method = "initGui", at = @At("RETURN"))
    private void chocotweak$extendTextureOptions(CallbackInfo ci) {
        if (usePlayerTexture == null)
            return;

        // Determine initial value
        int initialValue;
        if (npc instanceof IEasterEggCapable) {
            IEasterEggCapable easter = (IEasterEggCapable) npc;
            if (easter.isEasterEggNpc()) {
                // EasterEgg variants start at index 2
                initialValue = 2 + easter.getModelVariant();
            } else {
                // Original logic: Player=0, Entity=1
                initialValue = npc.hasPlayerTexture ? 0 : 1;
            }
        } else {
            initialValue = npc.hasPlayerTexture ? 0 : 1;
        }

        // Create replacement button with extended options
        GuiButtonMultiOptions newButton = new GuiButtonMultiOptions(
                usePlayerTexture.id,
                usePlayerTexture.x,
                usePlayerTexture.y,
                usePlayerTexture.width + 30, // Make button wider for longer names
                usePlayerTexture.height,
                TEXTURE_TYPES,
                Math.min(initialValue, TEXTURE_TYPES.length - 1));

        // Replace in buttonList (inherited from GuiScreen)
        int idx = this.buttonList.indexOf(usePlayerTexture);
        if (idx >= 0) {
            this.buttonList.set(idx, newButton);
        }
        usePlayerTexture = newButton;
    }

    /**
     * Before updateNPC saves, check if Easter Egg option is selected.
     * Send packet to server to sync Easter Egg settings for NBT persistence.
     */
    @Inject(method = "updateNPC", at = @At("HEAD"))
    private void chocotweak$saveEasterEggOption(CallbackInfo ci) {
        if (usePlayerTexture == null)
            return;
        if (!(npc instanceof IEasterEggCapable))
            return;

        IEasterEggCapable easter = (IEasterEggCapable) npc;
        int value = usePlayerTexture.value;

        boolean isEasterEgg = value >= 2;
        int variant = isEasterEgg ? (value - 2) : 0;

        if (isEasterEgg) {
            // Easter Egg mode
            easter.setEasterEggNpc(true);
            easter.setModelVariant(variant);
            // Keep hasPlayerTexture as false for Easter Egg
            npc.hasPlayerTexture = false;
        } else {
            // Normal mode (Player or Entity)
            easter.setEasterEggNpc(false);
            npc.hasPlayerTexture = (value == 0);
        }

        // 发送网络包到服务端，确保NBT正确保存
        ChocoNetwork.INSTANCE.sendToServer(new PacketEasterEggSync(
                npc.getEntityId(),
                isEasterEgg,
                variant));
    }
}
