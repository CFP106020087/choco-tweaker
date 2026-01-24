package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import com.chocotweak.bedrock.entity.EasterEggAnimController;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to add Easter Egg NPC capability to EntityHumanBase.
 * Easter Egg NPCs cannot attack, cannot be targeted, and use Bedrock model
 * rendering.
 * Uses DataParameter for client-server synchronization.
 */
@Mixin(value = EntityHumanBase.class, remap = false)
public abstract class MixinEntityHumanBaseEasterEgg extends Entity implements IEasterEggCapable {

    // DataParameters for network sync
    @Unique
    private static final DataParameter<Boolean> EASTER_EGG_FLAG = EntityDataManager.createKey(EntityHumanBase.class,
            DataSerializers.BOOLEAN);

    @Unique
    private static final DataParameter<Integer> EASTER_EGG_VARIANT = EntityDataManager.createKey(EntityHumanBase.class,
            DataSerializers.VARINT);

    @Unique
    private EasterEggAnimController chocotweak$animController;

    // Constructor required for extending Entity
    public MixinEntityHumanBaseEasterEgg() {
        super(null);
    }

    // ==================== DataManager Registration ====================

    @Inject(method = "entityInit", at = @At("TAIL"))
    private void chocotweak$registerDataParameters(CallbackInfo ci) {
        this.dataManager.register(EASTER_EGG_FLAG, false);
        this.dataManager.register(EASTER_EGG_VARIANT, 0);
    }

    // ==================== IEasterEggCapable Implementation ====================

    @Override
    public boolean isEasterEggNpc() {
        return this.dataManager.get(EASTER_EGG_FLAG);
    }

    @Override
    public void setEasterEggNpc(boolean easterEgg) {
        this.dataManager.set(EASTER_EGG_FLAG, easterEgg);
        if (easterEgg && chocotweak$animController == null) {
            chocotweak$animController = new EasterEggAnimController();
        }
    }

    @Override
    public EasterEggAnimController getEasterEggController() {
        if (chocotweak$animController == null) {
            chocotweak$animController = new EasterEggAnimController();
        }
        return chocotweak$animController;
    }

    @Override
    public int getModelVariant() {
        return this.dataManager.get(EASTER_EGG_VARIANT);
    }

    @Override
    public void setModelVariant(int variant) {
        this.dataManager.set(EASTER_EGG_VARIANT, Math.max(0, Math.min(14, variant)));
    }

    @Override
    public void triggerEasterEggReaction() {
        if (isEasterEggNpc() && chocotweak$animController != null) {
            chocotweak$animController.triggerReaction();
        }
    }

    // ==================== NBT Persistence ====================

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    private void chocotweak$writeToNBT(NBTTagCompound nbt, CallbackInfo ci) {
        nbt.setBoolean("EasterEggNpc", isEasterEggNpc());
        nbt.setInteger("EasterEggVariant", getModelVariant());
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    private void chocotweak$readFromNBT(NBTTagCompound nbt, CallbackInfo ci) {
        setEasterEggNpc(nbt.getBoolean("EasterEggNpc"));
        setModelVariant(nbt.getInteger("EasterEggVariant"));
    }
}
