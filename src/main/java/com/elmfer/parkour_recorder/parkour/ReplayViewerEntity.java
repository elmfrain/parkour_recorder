package com.elmfer.parkour_recorder.parkour;

import com.elmfer.parkour_recorder.render.GraphicsHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * A client only entity used during replay (in the Timeline GUI).
 * The {@code setState()} method is used for positioning the entity using two {@code ParkourFrame}s.
 **/
public class ReplayViewerEntity extends Player
{
	protected static Minecraft mc = Minecraft.getInstance();
	private Input movementInput = new Input();
	
	/**Init dummy player entity**/
	public ReplayViewerEntity()
	{
		super(mc.level, new BlockPos(mc.player.getPosition(1.0f)), 0.0f, mc.player.getGameProfile());
	}
	
	/**Position the viewer entity with previous and current frame information.**/
	public void setState(ParkourFrame frame, ParkourFrame prevFrame, float partialTicks)
	{
		//partialTicks = 0.0f;
		//Set position and set inputs
		double posX = GraphicsHelper.lerp(partialTicks, prevFrame.posX, frame.posX);
		double posY = GraphicsHelper.lerp(partialTicks, prevFrame.posY, frame.posY);
		double posZ = GraphicsHelper.lerp(partialTicks, prevFrame.posZ, frame.posZ);
		setPos(posX, posY, posZ);
		frame.setMovementInput(movementInput, this);
		
		xo = getX();
		yo = getY();
		zo = getZ();
		
		//Set head rotations
		yHeadRot = GraphicsHelper.lerp(partialTicks, prevFrame.headYaw, frame.headYaw);
		setYRot(yHeadRot);
		
		setXRot(GraphicsHelper.lerp(partialTicks, prevFrame.headPitch, frame.headPitch));
		
		//Set the player's arm rotation equal to the frames'
		float handYawOffset = GraphicsHelper.lerp(partialTicks, prevFrame.armYawOffset, frame.armYawOffset);
		float handPitchOffset = GraphicsHelper.lerp(partialTicks, prevFrame.armPitchOffset, frame.armPitchOffset);
		mc.player.yBobO = mc.player.yBob = mc.player.yHeadRot - handYawOffset;
		mc.player.xBobO = mc.player.xBob = mc.player.getXRot() - handPitchOffset;
		
		//Updates entity to prevent movement glitches
        xxa = movementInput.leftImpulse;
        zza = movementInput.forwardImpulse;
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
