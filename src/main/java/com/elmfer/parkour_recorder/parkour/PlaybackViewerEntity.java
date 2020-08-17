package com.elmfer.parkour_recorder.parkour;

import com.elmfer.parkour_recorder.render.GraphicsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovementInput;

public class PlaybackViewerEntity extends EntityPlayer
{
	protected static Minecraft mc = Minecraft.getMinecraft();
	private MovementInput movementInput = new MovementInput();
	
	public PlaybackViewerEntity()
	{
		super(mc.world, mc.player.getGameProfile());
	}
	
	public void setState(ParkourFrame frame, ParkourFrame prevFrame, float partialTicks)
	{
		double posX = GraphicsHelper.lerp(partialTicks, prevFrame.posX, frame.posX);
		double posY = GraphicsHelper.lerp(partialTicks, prevFrame.posY, frame.posY);
		double posZ = GraphicsHelper.lerp(partialTicks, prevFrame.posZ, frame.posZ);
		
		setPositionAndUpdate(posX, posY, posZ);
		frame.setInput(movementInput, this);
		
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		
		rotationYawHead = rotationYaw = GraphicsHelper.lerp(partialTicks, prevFrame.headYaw, frame.headYaw);
		rotationPitch = GraphicsHelper.lerp(partialTicks, prevFrame.headPitch, frame.headPitch);
		
		//super.updateEntityActionState();
		this.moveStrafing = this.movementInput.moveStrafe;
        this.moveForward = this.movementInput.moveForward;
        this.isJumping = this.movementInput.jump;
        onUpdate();
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
