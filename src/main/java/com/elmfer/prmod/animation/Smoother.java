package com.elmfer.prmod.animation;

public class Smoother {
    private double acceleration = 0.0;
    private double velocity = 0.0;
    private long previousTime = 0;
    private long currentTime = 0;

    private boolean grabbed = false;
    private double grabbingTo = 0.0;

    private double value = 0.0;

    private boolean springy = false;
    private double speed = 10.0;
    private double friction = 1.0;

    public void grab() {
        grabbed = true;
    }

    public void grab(double grabTo) {
        grabbed = true;
        grabbingTo = grabTo;
    }

    public void setValueAndGrab(double value) {
        grabbed = true;
        grabbingTo = value;
        this.value = value;
    }

    public void release() {
        grabbed = false;
    }

    public boolean isGrabbed() {
        return grabbed;
    }

    public boolean isSpringy() {
        return springy;
    }

    public void setSpringy(boolean springy) {
        this.springy = springy;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getFriction() {
        return friction;
    }

    public void setFriction(double friction) {
        this.friction = friction;
    }

    public double grabbingTo() {
        return grabbingTo;
    }

    public double getValue() {
        update();
        return value;
    }

    public float getValuef() {
        update();
        return (float) value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    private void update() {
        previousTime = currentTime;
        currentTime = System.nanoTime();
        if (previousTime == 0)
            previousTime = currentTime;

        double delta = (currentTime - previousTime) / 1.0e9;
        delta = Math.min(delta, 0.5);

        if (grabbed) {
            if (springy) {
                acceleration = (grabbingTo - value) * Math.abs(speed) * 32.0;
                velocity += acceleration * delta;
                velocity *= Math.pow(0.0025 / speed, delta);
            } else
                velocity = (grabbingTo - value) * Math.abs(speed);
        }

        value += velocity * delta;
        velocity *= Math.pow(0.0625 / (speed * friction), delta);
    }
}