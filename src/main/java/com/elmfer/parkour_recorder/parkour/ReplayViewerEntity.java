package com.elmfer.parkour_recorder.parkour;

import com.elmfer.parkour_recorder.render.GraphicsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovementInput;

/**
 * A client only entity used during replay (in the Timeline GUI).
 * The {@code setState()} method is used for positioning the entity using two {@code ParkourFrame}s.
 **/
public class ReplayViewerEntity extends EntityPlayer
{
	protected static Minecraft mc = Minecraft.getMinecraft();
	private MovementInput movementInput = new MovementInput();
	
	/**Init dummy player entity**/
	public ReplayViewerEntity()
	{
		super(mc.world, mc.player.getGameProfile());
	}
	
	/**Position the viewer entity with previous and current frame information.**/
	public void setState(ParkourFrame frame, ParkourFrame prevFrame, float partialTicks)
	{
		//Set position and set inputs
		double posX = GraphicsHelper.lerp(partialTicks, prevFrame.posX, frame.posX);
		double posY = GraphicsHelper.lerp(partialTicks, prevFrame.posY, frame.posY);
		double posZ = GraphicsHelper.lerp(partialTicks, prevFrame.posZ, frame.posZ);
		setPositionAndUpdate(posX, posY, posZ);
		frame.setMovementInput(movementInput, this);
		
		//Set previous values to prevent movement glitches
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		
		//Set head rotations
		rotationYawHead = rotationYaw = GraphicsHelper.lerp(partialTicks, prevFrame.headYaw, frame.headYaw);
		rotationPitch = GraphicsHelper.lerp(partialTicks, prevFrame.headPitch, frame.headPitch);
		
		//Set the player's arm rotation equal to the frames'
		float handYawOffset = GraphicsHelper.lerp(partialTicks, prevFrame.armYawOffset, frame.armYawOffset);
		float handPitchOffset = GraphicsHelper.lerp(partialTicks, prevFrame.armPitchOffset, frame.armPitchOffset);
		mc.player.prevRenderArmYaw = mc.player.renderArmYaw = mc.player.rotationYawHead - handYawOffset;
		mc.player.prevRenderArmPitch = mc.player.renderArmPitch = mc.player.rotationPitch - handPitchOffset;
		
		//Updates entity to prevent movement glitches
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
