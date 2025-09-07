package com.elmfer.prmod.parkour;

import net.minecraft.client.input.Input;
import net.minecraft.util.math.Vec2f;

public class ControlledInput extends Input {

    private static float getMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0F;
        } else {
            return positive ? 1.0F : -1.0F;
        }
    }
    
    @Override
    public void tick() {
        float f = getMovementMultiplier(this.playerInput.forward(), this.playerInput.backward());
        float g = getMovementMultiplier(this.playerInput.left(), this.playerInput.right());
        this.movementVector = new Vec2f(g, f).normalize();
    }
}
