package com.elmfer.parkour_recorder.parkour;

import net.minecraft.client.player.Input;

public class ControlledMovementInput extends Input {
	
	@Override
	public void tick(boolean p_225607_1_)
	{
		 this.forwardImpulse = this.up == this.down ? 0.0F : (this.up ? 1.0F : -1.0F);
		 this.leftImpulse = this.left == this.right ? 0.0F : (this.left ? 1.0F : -1.0F);
		 if (p_225607_1_)
		 {
		    this.leftImpulse = (float)((double)this.leftImpulse * 0.3D);
		    this.forwardImpulse = (float)((double)this.forwardImpulse * 0.3D);
		 }
	}
}
