package com.elmfer.prmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public interface EntityMixins {
	@Accessor("pos")
	public void setPosDirect(Vec3d pos);
	
	@Accessor("dimensions")
	public EntityDimensions getDimensions();
}
