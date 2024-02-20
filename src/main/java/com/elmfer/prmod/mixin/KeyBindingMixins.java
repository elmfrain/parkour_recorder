package com.elmfer.prmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.option.KeyBinding;

@Mixin(KeyBinding.class)
public interface KeyBindingMixins {
    @Accessor("timesPressed")
    public void setTimesPressed(int timesPressed);
    
    @Accessor("timesPressed")
    public int getTimesPressed();
}
