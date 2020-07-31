package com.elmfer.parkour_recorder.parkour;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.MathHelper;

public class PlaybackViewerEntity extends PlayerEntity
{
	private MovementInput movementInput = new MovementInput();
	
	public PlaybackViewerEntity()
	{
		super(Minecraft.getInstance().world, Minecraft.getInstance().player.getGameProfile());
	}
	
	public void setState(ParkourFrame frame, ParkourFrame prevFrame, float partialTicks)
	{
		double posX = MathHelper.lerp(partialTicks, prevFrame.posX, frame.posX);
		double posY = MathHelper.lerp(partialTicks, prevFrame.posY, frame.posY);
		double posZ = MathHelper.lerp(partialTicks, prevFrame.posZ, frame.posZ);
		
		setPosition(posX, posY, posZ);
		frame.setInput(movementInput, this);
		
		prevPosX = getPosX();
		prevPosY = getPosY();
		prevPosZ = getPosZ();
		
		rotationYawHead = rotationYaw = MathHelper.lerp(partialTicks, prevFrame.headYaw, frame.headYaw);
		rotationPitch = MathHelper.lerp(partialTicks, prevFrame.headPitch, frame.headPitch);
		
		movementInput.func_223135_b();
		movementInput.func_225607_a_(isCrouching() || isVisuallySwimming());
		super.updateEntityActionState();
		this.moveStrafing = this.movementInput.moveStrafe;
        this.moveForward = this.movementInput.moveForward;
        this.isJumping = this.movementInput.jump;
        super.tick();
	}
	
	@Override
	public boolean isSleeping() {
		return false;
	}
	
	@Override
	public boolean isSpectator()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCreative()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
