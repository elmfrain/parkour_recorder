package com.elmfer.prmod.parkour;

import net.minecraft.client.input.Input;

public class ControlledInput extends Input {

    @Override
    public void tick(boolean slowDown, float slowDownFactor) {
        this.movementForward = pressingForward == pressingBack ? 0.0f : (pressingForward ? 1.0f : -1.0f);
        this.movementSideways = pressingLeft == pressingRight ? 0.0f : (pressingLeft ? 1.0f : -1.0f);
        if (slowDown) {
            this.movementSideways *= slowDownFactor;
            this.movementForward *= slowDownFactor;
        }
    }
}
