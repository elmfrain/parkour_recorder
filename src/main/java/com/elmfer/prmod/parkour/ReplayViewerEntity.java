package com.elmfer.prmod.parkour;

import com.elmfer.prmod.render.GraphicsHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.entity.player.PlayerEntity;

/**
 * A client only entity used during replay (in the Timeline GUI).
 * The {@code setState()} method is used for positioning the entity using two {@code ParkourFrame}s.
 **/
public class ReplayViewerEntity extends PlayerEntity
{
	protected static MinecraftClient mc = MinecraftClient.getInstance();
	private Input movementInput = new Input();
	
	/**Init dummy player entity**/
	public ReplayViewerEntity()
	{
		super(mc.world, mc.player.getBlockPos(), 0.0f, mc.player.getGameProfile());
	}
	
	/**Position the viewer entity with previous and current frame information.**/
	public void setState(Frame frame, Frame prevFrame, float partialTicks)
	{
		//partialTicks = 0.0f;
		//Set position and set inputs
		double posX = GraphicsHelper.lerp(partialTicks, prevFrame.posX, frame.posX);
		double posY = GraphicsHelper.lerp(partialTicks, prevFrame.posY, frame.posY);
		double posZ = GraphicsHelper.lerp(partialTicks, prevFrame.posZ, frame.posZ);
		setPos(posX, posY, posZ);
		frame.setMovementInput(movementInput, this);
		
		prevX = getX();
		prevY = getY();
		prevZ = getZ();
		
		//Set head rotations
		headYaw = GraphicsHelper.lerp(partialTicks, prevFrame.headYaw, frame.headYaw);
		setYaw(headYaw);
		
		setPitch(GraphicsHelper.lerp(partialTicks, prevFrame.headPitch, frame.headPitch));
		
		//Set the player's arm rotation equal to the frames'
		float handYawOffset = GraphicsHelper.lerp(partialTicks, prevFrame.armYawOffset, frame.armYawOffset);
		float handPitchOffset = GraphicsHelper.lerp(partialTicks, prevFrame.armPitchOffset, frame.armPitchOffset);
		mc.player.renderYaw = mc.player.lastRenderYaw = mc.player.headYaw - handYawOffset;
		mc.player.renderPitch = mc.player.lastRenderPitch = mc.player.getPitch() - handPitchOffset;
		
		//Updates entity to prevent movement glitches
        sidewaysSpeed = movementInput.movementSideways;
        forwardSpeed = movementInput.movementForward;
        jumping = this.movementInput.jumping;
        tick();
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
