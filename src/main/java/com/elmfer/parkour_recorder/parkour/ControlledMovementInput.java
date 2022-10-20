package com.elmfer.parkour_recorder.parkour;

import net.minecraft.client.player.Input;

public class ControlledMovementInput extends Input {
	
	@Override
	public void tick(boolean p1, float p2)
	{
		 this.forwardImpulse = this.up == this.down ? 0.0F : (this.up ? 1.0F : -1.0F);
		 this.leftImpulse = this.left == this.right ? 0.0F : (this.left ? 1.0F : -1.0F);
		 if (p1)
		 {
		    this.leftImpulse *= p2;
		    this.forwardImpulse *= p2;
		 }
	}
}
