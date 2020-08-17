package com.elmfer.parkour_recorder.parkour;

import net.minecraft.util.MovementInput;

public class ControlledMovementInput extends MovementInput {
	
	@Override
	public void updatePlayerMoveState()
	{
		 this.moveForward = this.forwardKeyDown == this.backKeyDown ? 0.0F : (this.forwardKeyDown ? 1.0F : -1.0F);
		 this.moveStrafe = this.leftKeyDown == this.rightKeyDown ? 0.0F : (this.leftKeyDown ? 1.0F : -1.0F);
		 if (sneak)
		 {
		    this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
		    this.moveForward = (float)((double)this.moveForward * 0.3D);
		 }
	}
}
