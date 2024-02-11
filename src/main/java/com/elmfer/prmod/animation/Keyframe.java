package com.elmfer.prmod.animation;

import java.util.function.Function;

public class Keyframe {

    private final Function<Double, Double> timeShader;
    private final double value;
    private final double fracTimeStamp;

    public Keyframe(double fracTimeStamp, double value) {

        this.fracTimeStamp = fracTimeStamp;
        this.value = value;
        this.timeShader = Easing.getDefault().getFunction();
    }

    public Keyframe(double fracTimeStamp, double value, Function<Double, Double> easingFunc) {

        this.fracTimeStamp = fracTimeStamp;
        this.value = value;
        this.timeShader = easingFunc;
    }

    public Keyframe(double fracTimeStamp, double value, Easing easingEnum) {

        this.fracTimeStamp = fracTimeStamp;
        this.value = value;
        this.timeShader = easingEnum.getFunction();
    }

    public double getFracTimeStamp() {

        return fracTimeStamp;
    }

    public Function<Double, Double> getValueShader() {

        return timeShader;
    }

    public double getValue() {

        return value;
    }

}
