package com.elmfer.parkour_recorder.parkour;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class ReplayViewerEntity extends PlayerEntity
{
	private MovementInput movementInput = new MovementInput();
	
	public ReplayViewerEntity()
	{
		super(Minecraft.getInstance().world, new BlockPos(Minecraft.getInstance().player.getPositionVec()), 0.0f, Minecraft.getInstance().player.getGameProfile());
	}
	
	public void setState(ParkourFrame frame, ParkourFrame prevFrame, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		
		//Set position and inputs
		double posX = MathHelper.lerp(partialTicks, prevFrame.posX, frame.posX);
		double posY = MathHelper.lerp(partialTicks, prevFrame.posY, frame.posY);
		double posZ = MathHelper.lerp(partialTicks, prevFrame.posZ, frame.posZ);
		setPosition(posX, posY, posZ);
		frame.setMovementInput(movementInput, this);
		
		//Set previous values to prevent movement glitches
		prevPosX = getPosX();
		prevPosY = getPosY();
		prevPosZ = getPosZ();
		
		//Set head rotations
		rotationYawHead = rotationYaw = MathHelper.lerp(partialTicks, prevFrame.headYaw, frame.headYaw);
		rotationPitch = MathHelper.lerp(partialTicks, prevFrame.headPitch, frame.headPitch);
		
		//Set the player's arm rotation
		float handYawOffset = MathHelper.lerp(partialTicks, prevFrame.armYawOffset, frame.armYawOffset);
		float handPitchOffset = MathHelper.lerp(partialTicks, prevFrame.armPitchOffset, frame.armPitchOffset);
		mc.player.prevRenderArmYaw = mc.player.renderArmYaw = mc.player.rotationYawHead - handYawOffset;
		mc.player.prevRenderArmPitch = mc.player.renderArmPitch = mc.player.rotationPitch - handPitchOffset;
		
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
